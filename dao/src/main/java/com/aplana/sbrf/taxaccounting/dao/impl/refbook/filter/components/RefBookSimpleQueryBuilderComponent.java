package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.DBInfo;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

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
    public PreparedStatementData psGetChildrenRecordsQuery(RefBook refBook, Long parentId, Date version, RefBookAttribute sortAttribute,
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
            fields.append(", \"record_version_from\", (SELECT MIN(VERSION) FROM ");
            fields.append(refBook.getTableName());
            fields.append(" rbo1 where rbo1.VERSION>\"record_version_from\") \"record_version_to\"");
        }
        if (parentId != null) {
            ps.appendQuery(fields.toString());
            ps.append(", CASE WHEN EXISTS(SELECT 1 FROM t WHERE t.record_id = rbo.record_id AND lvl > 1) THEN 1 ELSE 0 END AS \"")
            .append(RefBook.RECORD_HAS_CHILD_ALIAS).append("\" ");
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
    public PreparedStatementData psGetRecordsQuery(RefBook refBook, Long recordId, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
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
            if (!RefBook.SYSTEM_ALIASES.contains(attribute.getAlias())) {
                ps.appendQuery(", frb.");
                ps.appendQuery(attribute.getAlias());
            }
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

    public PreparedStatementData psGetMatchedRecordsByUniqueAttributes(@NotNull RefBook refBook, Long uniqueRecordId, @NotNull RefBookRecord record,
                                                                       @NotNull Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groupsUniqueAttributesValues) {
        List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues = groupsUniqueAttributesValues.get(1);

        PreparedStatementData sql = new PreparedStatementData("SELECT r.record_id as id, ");
        appendNameColumn(sql, uniqueAttributesValues);
        sql.append("FROM ").append(refBook.getTableName()).append(" r\n");
        sql.append("WHERE r.status = 0\nAND (\n");
        appendWhereCondition(record, sql, uniqueAttributesValues);
        sql.append(")");
        if (uniqueRecordId != null) {
            sql.append("\nAND r.record_id != ").append(uniqueRecordId);
        }
        return sql;
    }

    private void appendNameColumn(PreparedStatementData sql, List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues) {
        for (int j = 0; j < uniqueAttributesValues.size(); j++) {
            RefBookAttribute attribute = uniqueAttributesValues.get(j).getFirst();

            sql.append("'").append(attribute.getName()).append("'");
            appendIfAttributeIsNotLast(sql, uniqueAttributesValues, j, "||','||");
        }
        sql.append(" as name\n");
    }

    private void appendWhereCondition(RefBookRecord record, PreparedStatementData sql,
                                      List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues) {
        for (int j = 0; j < uniqueAttributesValues.size(); j++) {
            RefBookAttribute attribute = uniqueAttributesValues.get(j).getFirst();
            sql.addNamedParam(attribute.getAlias(), record.getValues().get(attribute.getAlias()).getValue());

            sql.append("r.").append(attribute.getAlias()).append(" = :").append(attribute.getAlias()).append("\n");
            appendIfAttributeIsNotLast(sql, uniqueAttributesValues, j, "OR ");
        }
    }

    private void appendIfAttributeIsNotLast(PreparedStatementData sql, List list, int index, String query) {
        if (index < list.size() - 1) {
            sql.append(query);
        }
    }

    private static final String CHECK_CONFLICT_VALUES_VERSIONS =
            "with conflictRecord as (select id, record_id from %1$s where %2$s),\n" +
            "allRecordsInConflictGroup AS (SELECT distinct r.id, r.record_id, r.version "+
            "FROM %1$s r JOIN conflictRecord cr ON (r.RECORD_ID = cr.RECORD_ID AND r.status != -1)" +
            "),\n" +
            "recordsByVersion as (select ar.id, ar.record_id, ar.version, row_number() over(%3$s) rn from allRecordsInConflictGroup ar),\n" +
            "versionInfo as (select rv.ID, rv.VERSION versionFrom, rv2.version - interval '1' day versionTo from conflictRecord cr, recordsByVersion rv " +
            "left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.ID=cr.ID)" +
            "select ID from versionInfo where (\n" +
            "\tversionTo IS NOT NULL and (\n" +
            "\t\t(:versionTo IS NULL and versionTo >= :versionFrom) or\n" +
            "\t\t(versionFrom <= :versionFrom and versionTo >= :versionFrom) or \n" +
            "\t\t(versionFrom >= :versionFrom and versionFrom <= :versionTo)\n" +
            "\t)\n" +
            ") or (\n" +
            "\tversionTo IS NULL and (\n" +
            "\t\tversionFrom <= :versionFrom or\n" +
            "\t\t(versionFrom >= :versionFrom and (:versionTo IS NULL or versionFrom <= :versionTo))\n" +
            "\t)\n" +
            ")";

    private static final String CHECK_CONFLICT_VALUES_VERSIONS_PARTITION = "partition by ar.RECORD_ID order by ar.version";


    public PreparedStatementData psCheckConflictValuesVersions(RefBook refBook, List<Long> recordIds, Date versionFrom, Date versionTo){
        String partition = isSupportOver() ? CHECK_CONFLICT_VALUES_VERSIONS_PARTITION : "";
        String query = String.format(CHECK_CONFLICT_VALUES_VERSIONS, refBook.getTableName(),
                transformToSqlInStatement("ID", recordIds), partition);

        PreparedStatementData sql = new PreparedStatementData(query);
        sql.addNamedParam("versionFrom", versionFrom);
        sql.addNamedParam("versionTo", versionTo);

        return sql;
    }

    private static final String CHECK_PARENT_CONFLICT = "with currentRecord as (select id, record_id, version from %1$s where id = :parentId),\n" +
            "nextVersion as (select min(r.version) as version from %1$s r, currentRecord cr where r.version > cr.version and r.record_id=cr.record_id and r.status != -1),\n" +
            "allRecords as (select cr.id, cr.version as versionStart, nv.version - interval '1' day as versionEnd from currentRecord cr, nextVersion nv)\n" +
            "select distinct id,\n" +
            "case\n" +
            "\twhen (versionEnd is not null and (:versionTo is null or :versionTo > versionEnd)) then 1\n" +
            "\twhen (:versionFrom < versionStart) then -1\n" +
            "\telse 0\n" +
            "end as result\n" +
            "from allRecords";

    public PreparedStatementData psCheckParentConflict(RefBook refBook, Long parentId, Date versionFrom, Date versionTo){
        PreparedStatementData sql = new PreparedStatementData(String.format(CHECK_PARENT_CONFLICT, refBook.getTableName()));
        sql.addNamedParam("parentId", parentId);
        sql.addNamedParam("versionFrom", versionFrom);
        sql.addNamedParam("versionTo", versionTo);
        return sql;
    }

    private static final String CHECK_CROSS_VERSIONS =
            "with allVersions as (select r.id, r.record_id, r.version from %1$s r where status != -1 and record_id=:recordId and (:excludedRecordId is null or id != :excludedRecordId)),\n" +
            "recordsByVersion as (select r.id, r.record_id, r.status, r.version, row_number() over(%2$s) rn from %1$s r, allVersions av where r.id=av.id and r.status != -1),\n" +
            "versionInfo as (select rv.rn NUM, rv.ID, rv.VERSION, rv.status, rv2.version - interval '1' day nextVersion,rv2.status nextStatus from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn)\n" +
            "select num, id, version, status, nextversion, nextstatus, \n" +
            "case\n" +
            "  when (status=0 and (\n" +
            "  \t(:versionTo is null and (\n" +
            "  \t\t(nextversion is not null and nextversion >= :versionFrom) or \n" +
            "\t\t(nextversion is null and version >= :versionFrom)\n" +
            "  \t)) or (:versionTo is not null and (\n" +
            "  \t\t(version <= :versionFrom and nextversion is not null and nextversion >= :versionFrom) or \n" +
            "  \t\t(version >= :versionFrom and version <= :versionTo)\n" +
            "  \t))\n" +
            "  )) then 1\n" +
            "  when (status=0 and nextversion is null and version < :versionFrom) then 2\n" +
            "  when (status=2 and (:versionTo is not null and version >= :versionFrom and version < :versionTo and nextversion is not null and nextversion > :versionTo)) then 3 \n" +
            "  when (status=2 and (\n" +
            "  \t(nextversion is not null and :versionTo is null and version > :versionFrom) or  \n" +
            "  \t(version = :versionFrom) or \n" +
            "  \t(nextversion is null and version >= :versionFrom and (:versionTo is null or :versionTo >= version))\n" +
            "  )) then 4\n" +
            "  else 0\n" +
            "end as result\n" +
            "from versionInfo";

    private static final String CHECK_CROSS_VERSIONS_PARTITION = "partition by r.record_id order by r.version";

    public PreparedStatementData psCheckCrossVersions(RefBook refBook){
        String partition = isSupportOver() ? CHECK_CROSS_VERSIONS_PARTITION : "";
        return new PreparedStatementData(String.format(CHECK_CROSS_VERSIONS, refBook.getTableName(), partition));
    }

    private static final String IS_VERSION_USED_LIKE_PARENT =
            "select r.version as version, \n" +
            "  (SELECT min(version) - interval '1' DAY FROM %1$s rn WHERE rn.record_id = r.record_id AND rn.version > r.version) AS versionEnd\n" +
            "from %1$s r where r.%2$s = :parentId and r.version >= :versionFrom and r.status != -1";

    public PreparedStatementData psVersionUsedLikeParent(RefBook refBook){
        String query = String.format(IS_VERSION_USED_LIKE_PARENT, refBook.getTableName(), RefBook.RECORD_PARENT_ID_ALIAS);
        return new PreparedStatementData(query);
    }

    public PreparedStatementData psGetNextVersion(RefBook refBook, Date version, String filter){

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, simpleFilterTreeListener);

        PreparedStatementData sql = new PreparedStatementData();
        sql.appendQuery("select min(frb.version) as version\n");
        sql.appendQuery(getFromStatementForGetNextVersion(refBook, filterPS).toString());
        sql.appendQuery("where\n frb.version > :version\n and status <> -1\n");
        sql.addNamedParam("version", version);

        if (filterPS.getQuery().length() > 0) {
            sql.appendQuery(" and\n (");
            sql.appendQuery(filterPS.getQuery().toString());
            sql.appendQuery(")\n");
        }
        return sql;
    }

    private StringBuilder getFromStatementForGetNextVersion(RefBook refBook, PreparedStatementData filterPS) {
        StringBuilder fromSql = new StringBuilder();
        fromSql.append("from ");
        fromSql.append(refBook.getTableName());
        fromSql.append(" frb\n");

        if (filterPS.getJoinPartsOfQuery() != null) {
            fromSql.append(filterPS.getJoinPartsOfQuery());
        }
        return fromSql;
    }

    public PreparedStatementData psCreateFakeRecordVersion(RefBook refBook, Long recordId, Date version) {
        PreparedStatementData sql = new PreparedStatementData();
        List<RefBookAttribute> requiredAttributes = getRequiredAttributesListFromBook(refBook);

        sql.addNamedParam("recordId", recordId);
        sql.addNamedParam("version", new java.sql.Date(version.getTime()));

        sql.append("insert into ").append(refBook.getTableName()).append(" (id, record_id, version, status");
        for (RefBookAttribute requiredAttribute : requiredAttributes) {
            sql.append(", ").append(requiredAttribute.getAlias());
        }
        sql.append(")\n values (seq_ref_book_record.nextval, :recordId, :version, 2");
        for (RefBookAttribute requiredAttribute : requiredAttributes) {
            sql.append(", ");
            appendFakeAttributeValue(sql, requiredAttribute);
        }
        sql.append(")");
        return sql;
    }

    private void appendFakeAttributeValue(PreparedStatementData sql, RefBookAttribute requiredAttribute) {
        Date fakeDate = new java.sql.Date(0);
        sql.append(":").append(requiredAttribute.getAlias());

        switch (requiredAttribute.getAttributeType()) {
            case STRING:
                if (requiredAttribute.getAlias().equals("SOCIAL") ||
                        requiredAttribute.getAlias().equals("PENSION") ||
                        requiredAttribute.getAlias().equals("MEDICAL")
                        ) {
                    sql.addNamedParam(requiredAttribute.getAlias(), 1);
                } else {
                    sql.addNamedParam(requiredAttribute.getAlias(), "-");
                }
                break;
            case NUMBER:
                sql.addNamedParam(requiredAttribute.getAlias(), 1);
                break;
            case DATE:
                sql.addNamedParam(requiredAttribute.getAlias(), fakeDate);
                break;
            case REFERENCE:
                sql.addNamedParam(requiredAttribute.getAlias(), 42L);
                break;
        }
    }

    private List<RefBookAttribute> getRequiredAttributesListFromBook(RefBook refBook) {
        List<RefBookAttribute> requiredAttributes = new ArrayList<RefBookAttribute>();
        for(RefBookAttribute attribute : refBook.getAttributes()){
            if (attribute.isRequired()) {
                requiredAttributes.add(attribute);
            }
        }
        return requiredAttributes;
    }

    public PreparedStatementData psCreateRecordVersion(final RefBook refBook) {
        PreparedStatementData sql = new PreparedStatementData();
        sql.append("insert into ").append(refBook.getTableName()).append(" (id, record_id, version, status");
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", ").append(attribute.getAlias());
        }
        sql.append(") values (:id, :recordId, :version, :status");
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", :").append(attribute.getAlias());
        }
        sql.append(")");



        return sql;
    }



}
