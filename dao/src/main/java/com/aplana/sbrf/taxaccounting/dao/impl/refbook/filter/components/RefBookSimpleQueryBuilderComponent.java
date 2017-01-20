package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.DBInfo;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Компонент для построения SQL запросов для DAO {@link com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookSimpleDaoImpl}
 *
 */
@Component
public class RefBookSimpleQueryBuilderComponent {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DBInfo dbInfo;

    /**
     *
     * @param refBook справочник
     * @param parentId уникальный идентификатор версии записи справочника (фактически поле ID). Используется только при получении всех версий записи
     * @param version дата актуальности, по которой определяется период актуальности и соответственно версия записи, которая в нем действует
     *                Если = null, значит будет выполняться получение всех версий записи
     *                Иначе выполняется получение всех записей справочника, активных на указанную дату
     * @param sortAttribute
     * @param filter
     * @param pagingParams
     * @param isSortAscending

     * @return
     */
    public PreparedStatementData getChildrenRecordsQuery(RefBook refBook, Long parentId, Date version, RefBookAttribute sortAttribute,
                                                         String filter, PagingParams pagingParams, boolean isSortAscending) {

        PreparedStatementData ps = new PreparedStatementData();

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, simpleFilterTreeListener);

        ps.appendQuery("WITH t AS ");
        ps.appendQuery("(SELECT ");
        if (parentId != null) {
            ps.appendQuery("CONNECT_BY_ROOT frb.id as \"RECORD_ID\"");

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                ps.appendQuery(", CONNECT_BY_ROOT frb.");
                ps.appendQuery(attribute.getAlias());
                ps.appendQuery(" as \"");
                ps.appendQuery(attribute.getAlias());
                ps.appendQuery("\"");
            }
            if (version == null) {
                ps.appendQuery(", CONNECT_BY_ROOT frb.version AS \"record_version_from\"");
            }
            ps.appendQuery(", level as lvl ");
        } else {
            ps.appendQuery("frb.id");
        }

        ps.appendQuery(" FROM ");
        ps.appendQuery(refBook.getTableName());
        ps.appendQuery(" frb ");
        if (filterPS.getJoinPartsOfQuery() != null) {
            ps.appendQuery(filterPS.getJoinPartsOfQuery());
        }

        if (version != null) {
            ps.appendQuery(String.format(" WHERE frb.status = 0 and frb.version <= ? and " +
                    "not exists (select 1 from %s r2 where r2.record_id=frb.record_id and r2.status != -1 and r2.version between frb.version + interval '1' day and ?)\n", refBook.getTableName()));
            ps.addParam(version);
            ps.addParam(version);
        } else {
            ps.appendQuery(" WHERE frb.status = 0");
        }

        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" AND (");
            ps.appendQuery(filterPS.getQuery().toString());
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
            ps.appendQuery(") ");
        }
        if (parentId != null) {
            ps.appendQuery(" START WITH frb." + (parentId == null ? "PARENT_ID is null" : "PARENT_ID = " + parentId));
            ps.appendQuery(" CONNECT BY NOCYCLE PRIOR frb.ID = frb.PARENT_ID");
        }
        ps.appendQuery(")");

        ps.appendQuery(", res AS ");
        ps.appendQuery("(SELECT DISTINCT ");
        StringBuilder fields = new StringBuilder();
        fields.append("record_id");
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            fields.append(", ");
            fields.append(attribute.getAlias());
        }
        if (version == null) {
            fields.append(", \"record_version_from\", (SELECT MIN(VERSION) FROM " + refBook.getTableName() + " rbo1 where rbo1.VERSION>\"record_version_from\") \"record_version_to\" ");
        }
        if (parentId != null) {
            ps.appendQuery(fields.toString());
            ps.appendQuery(", CASE WHEN EXISTS(SELECT 1 FROM t WHERE t.record_id = rbo.record_id AND lvl > 1) THEN 1 ELSE 0 END AS \"" + RefBook.RECORD_HAS_CHILD_ALIAS + "\" ");
            ps.appendQuery(" FROM t rbo ");
        } else {
            ps.appendQuery(" frb.id as \"RECORD_ID\"");

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                ps.appendQuery(", frb.");
                ps.appendQuery(attribute.getAlias());
                ps.appendQuery(" as \"");
                ps.appendQuery(attribute.getAlias());
                ps.appendQuery("\"");
            }
            if (version == null) {
                ps.appendQuery(", frb.version AS \"record_version_from\"");
            }
            ps.appendQuery(" FROM ");
            ps.appendQuery(refBook.getTableName());
            ps.appendQuery(" frb ");

            ps.appendQuery("START WITH frb.id IN (SELECT id FROM t) \n");
            ps.appendQuery(" CONNECT BY PRIOR PARENT_ID = ID\n");
        }
        ps.appendQuery(")");

        ps.appendQuery("SELECT * FROM (");
        ps.appendQuery("SELECT ");
        ps.appendQuery(fields.toString());
        if (parentId != null) {
            ps.appendQuery(", " + RefBook.RECORD_HAS_CHILD_ALIAS);
        } else {
            ps.appendQuery(", CASE WHEN EXISTS(SELECT 1 FROM res res1 WHERE res.record_id = res1.parent_id) THEN 1 ELSE 0 END as " + RefBook.RECORD_HAS_CHILD_ALIAS);
        }
        if (isSupportOver() && sortAttribute != null) {
            ps.appendQuery(",");
            ps.appendQuery(" row_number()");
            ps.appendQuery(" over (order by ");
            ps.appendQuery(sortAttribute.getAlias());
            ps.appendQuery(isSortAscending ? " ASC" : " DESC");
            ps.appendQuery(")");
            ps.appendQuery(" as row_number_over\n");
        } else {
            ps.appendQuery(", rownum row_number_over\n");
        }
        ps.appendQuery(" FROM res\n");
        if (parentId == null) {
            ps.appendQuery("WHERE ");
            ps.appendQuery("PARENT_ID is null");
        }
        ps.appendQuery(")");

        appendPagingCondition(pagingParams, ps);

        return ps;
    }

    private static final String WITH_STATEMENT =
            "with t as (select max(version) version, record_id from %s r where status = 0 and version <= ?  and\n" +
                    "not exists (select 1 from %s r2 where r2.record_id=r.record_id and r2.status != -1 and r2.version between r.version + interval '1' day and ?)\n" +
                    "group by record_id)\n";

    private String sqlRecordVersions() {
        return "with currentRecord as (select id, record_id, version from %s where id=?),\n" +
                "recordsByVersion as (select r.ID, r.RECORD_ID, r.VERSION, r.STATUS, row_number() " +
                (isSupportOver() ? "over(partition by r.RECORD_ID order by r.version)" : "over()") +
                "rn from %s r, currentRecord cr where r.RECORD_ID=cr.RECORD_ID and r.status != -1), \n" +
                "t as (select rv.rn as row_number_over, rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";
    }

    private String sqlRecordVersionsByRecordId() {
        return "with recordsByVersion as (select r.ID, r.RECORD_ID, r.VERSION, r.STATUS, row_number() " +
                (isSupportOver() ? "over(partition by r.RECORD_ID order by r.version)" : "over()") +
                "rn from %s r where r.record_id=%d and r.status != -1), \n" +
                "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";
    }

    private String sqlRecordVersionsAll() {
        return "with recordsByVersion as (select r.ID, r.RECORD_ID, r.VERSION, r.STATUS, row_number() " +
                (isSupportOver() ? "over(partition by r.RECORD_ID order by r.version)" : "over()") +
                "rn from %s r where r.status != -1), \n" +
                "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";
    }

    /**
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор версии записи справочника (фактически поле ID). Используется только при получении всех версий записи
     * @param version дата актуальности, по которой определяется период актуальности и соответственно версия записи, которая в нем действует
     *                Если = null, значит будет выполняться получение всех версий записи
     *                Иначе выполняется получение всех записей справочника, активных на указанную дату
     * @param sortAttribute атррибут по которому сортируется выборка
     * @param filter параметры фильтрации
     * @param pagingParams параметры пагинации
     * @param isSortAscending порядок сортировки
     * @param onlyId флаг указывающий на то что в выборке будет только record_id а не полный список полей
     * @return
     */
    public PreparedStatementData getRecordsQuery(RefBook refBook, Long recordId, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
                                                 String filter, PagingParams pagingParams, boolean isSortAscending, boolean onlyId) {
        PreparedStatementData ps = new PreparedStatementData();
        if (version != null) {
            ps.appendQuery(String.format(WITH_STATEMENT, refBook.getTableName(), refBook.getTableName()));
            ps.addParam(version);
            ps.addParam(version);
        } else {
            if (uniqueRecordId != null) {
                ps.appendQuery(String.format(sqlRecordVersions(), refBook.getTableName(), refBook.getTableName()));
                ps.addParam(uniqueRecordId);
                ps.addParam(VersionedObjectStatus.NORMAL.getId());
            } else if (recordId != null){
                ps.appendQuery(String.format(sqlRecordVersionsByRecordId(), refBook.getTableName(), recordId));
                ps.addParam(VersionedObjectStatus.NORMAL.getId());
            } else {
                ps.appendQuery(String.format(sqlRecordVersionsAll(), refBook.getTableName()));
                ps.addParam(VersionedObjectStatus.NORMAL.getId());
            }
        }

        ps.appendQuery("SELECT * FROM (");
        if (onlyId) {
            ps.appendQuery("SELECT record_id FROM ");
        } else {
            ps.appendQuery("SELECT res.*, rownum row_number_over FROM ");
        }

        ps.appendQuery("(select frb.id as ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);

        if (version == null) {
            ps.appendQuery(",  t.version as ");
            ps.appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS);
            ps.appendQuery(",");

            ps.appendQuery("  t.versionEnd as ");
            ps.appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
        }

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", frb.");
            ps.appendQuery(attribute.getAlias());
        }
        ps.appendQuery(" FROM t, ");
        ps.appendQuery(refBook.getTableName());
        ps.appendQuery(" frb ");

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener =  applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getJoinPartsOfQuery() != null){
            ps.appendQuery(filterPS.getJoinPartsOfQuery());
        }
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE (");
            ps.appendQuery(filterPS.getQuery().toString());
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
            ps.appendQuery(") ");
        }

        if (filterPS.getQuery().length() > 0 ) {
            ps.appendQuery(" and ");
        } else {
            ps.appendQuery(" where ");
        }
        ps.appendQuery("(frb.version = t.version and frb.record_id = t.record_id)");

        if (sortAttribute != null) {
            ps.appendQuery(" order by ");
            ps.appendQuery("frb." + sortAttribute.getAlias());
            ps.appendQuery(isSortAscending ? " ASC":" DESC");
        } else {
            ps.appendQuery(" order by frb.id");
        }
        if (version == null) {
            ps.appendQuery(" , t.version\n");
        }

        ps.appendQuery(") res) ");

        appendPagingCondition(pagingParams, ps);
        return ps;
    }

    private void appendPagingCondition(PagingParams pagingParams, PreparedStatementData ps) {
        if (pagingParams != null) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
            ps.appendQuery(" BETWEEN ? AND ?");
            int startIndex = pagingParams.getStartIndex() == 0 ? 1 : pagingParams.getStartIndex();
            ps.addParam(startIndex);
            ps.addParam(startIndex + pagingParams.getCount() - 1);
        }
    }

    private boolean isSupportOver() {
        return dbInfo.isSupportOver();
    }

    public StringBuilder getMatchedRecordsByUniqueAttributes(RefBook refBook, Long uniqueRecordId, List<RefBookRecord> records,
                                                             List<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>> recordsGroupsUniqueAttributesValues,
                                                             List<Object> selectParams) {
        // Параметры для where
        List<Object> whereParams = new ArrayList<Object>();
//        whereParams.add(refBook.getId());

        StringBuilder sqlCaseBuilder = new StringBuilder("CASE\n");
        StringBuilder sqlFromBuilder = new StringBuilder("FROM  ");
        sqlFromBuilder.append(refBook.getTableName());
        sqlFromBuilder.append(" r\n");
        StringBuilder sqlWhereBuilder = new StringBuilder("WHERE r.status = 0 AND\n");

        // Максимальное количество self-join таблиц
        int maxTablesCount = 0;
        // OR по каждой записи
        for (int i = 0; i < records.size(); i++) {
            sqlWhereBuilder.append("(");
            RefBookRecord record = records.get(i);
            Map<String, RefBookValue> recordValues = record.getValues();
            Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groupsUniqueAttributesValues = recordsGroupsUniqueAttributesValues.get(i);
            // OR по группам уникальности
            for (Map.Entry<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groupUniqueAttributesValues : groupsUniqueAttributesValues.entrySet()) {
                sqlCaseBuilder.append("WHEN ");
                sqlWhereBuilder.append("(");
                List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues = groupUniqueAttributesValues.getValue();
                StringBuilder clauseForGroup = new StringBuilder();
                StringBuilder attrNameBuilder = new StringBuilder();
                // AND по уникальным аттрибутам группы
                for (int j = 0; j < uniqueAttributesValues.size(); j++) {
                    // Нумерация self-join таблиц
                    int tableNumber = j + 1;
                    String valuesTableName = "v" + tableNumber;
                    String attributesTableName = "a" + tableNumber;
                    if (maxTablesCount < uniqueAttributesValues.size() && maxTablesCount < tableNumber) {
                        sqlFromBuilder.append("JOIN ref_book_value ").append(valuesTableName).append(" ON ").append(valuesTableName).append(".record_id = r.id\n");
                        sqlFromBuilder.append("JOIN ref_book_attribute ").append(attributesTableName).append(" ON ").append(attributesTableName).append(".id = ").append(valuesTableName).append(".attribute_id\n");
                    }

                    attrNameBuilder.append(attributesTableName).append(".name");
                    Pair<RefBookAttribute, RefBookValue> pair = uniqueAttributesValues.get(j);
                    RefBookAttribute attribute = pair.getFirst();
                    String type = attribute.getAttributeType().toString() + "_VALUE";
                    // Здесь проставляем номера таблиц
                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        clauseForGroup.append(valuesTableName).append(".attribute_id = ? AND (UPPER(").append(valuesTableName).append(".").append(type).append(") = UPPER(?) ");
                    } else {
                        clauseForGroup.append(valuesTableName).append(".attribute_id = ? AND (").append(valuesTableName).append(".").append(type).append(" = ? ");
                    }
                    // добавляем проверку на null для необязательных уникальных полей
                    if (!attribute.isRequired()) {
                        clauseForGroup.append("OR (").append(valuesTableName).append(".").append(type).append(" IS NULL AND ? IS NULL)");
                    }
                    clauseForGroup.append(")");

                    /*************************************Добавление параметров****************************************/
                    selectParams.add(attribute.getId());
                    whereParams.add(attribute.getId());

                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        selectParams.add(recordValues.get(attribute.getAlias()).getStringValue());
                        whereParams.add(recordValues.get(attribute.getAlias()).getStringValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        selectParams.add(recordValues.get(attribute.getAlias()).getReferenceValue());
                        whereParams.add(recordValues.get(attribute.getAlias()).getReferenceValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                        selectParams.add(recordValues.get(attribute.getAlias()).getNumberValue());
                        whereParams.add(recordValues.get(attribute.getAlias()).getNumberValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                        selectParams.add(recordValues.get(attribute.getAlias()).getDateValue());
                        whereParams.add(recordValues.get(attribute.getAlias()).getDateValue());
                    }
                    if (!attribute.isRequired()) {
                        selectParams.add(selectParams.get(selectParams.size() - 1));
                        whereParams.add(whereParams.get(whereParams.size() - 1));
                    }
                    /**************************************************************************************************/

                    if (j < uniqueAttributesValues.size() - 1) {
                        clauseForGroup.append(" AND ");
                        attrNameBuilder.append(" || ', ' || ");
                    } else {
                        sqlCaseBuilder.append(clauseForGroup);
                        clauseForGroup.append(" AND (? IS NULL OR r.record_id != ?)");
                        whereParams.add(record.getRecordId());
                        whereParams.add(record.getRecordId());
                        attrNameBuilder.append("\n");
                    }
                }

                sqlCaseBuilder.append("THEN ").append(attrNameBuilder);

                sqlWhereBuilder.append(clauseForGroup);
                sqlWhereBuilder.append(")");
                if (groupUniqueAttributesValues.getKey() < groupsUniqueAttributesValues.size()) {
                    sqlWhereBuilder.append(" OR ");
                }
                if (maxTablesCount < uniqueAttributesValues.size()) maxTablesCount = uniqueAttributesValues.size();
            }

            sqlWhereBuilder.append(")\n");
            if (i < records.size() - 1) sqlWhereBuilder.append(" OR ");
        }

        sqlCaseBuilder.append(" END AS name\n");

        if (uniqueRecordId != null) {
            sqlWhereBuilder.append(" AND v1.record_id != ?");
            whereParams.add(uniqueRecordId);
        }

        // Собираем куски в запрос
        StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT v1.record_id as id,\n");
        sqlBuilder.append(sqlCaseBuilder);
        sqlBuilder.append(sqlFromBuilder);
        sqlBuilder.append(sqlWhereBuilder);

        selectParams.addAll(whereParams);
        return sqlBuilder;
    }
}