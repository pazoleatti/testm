package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.DBInfo;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Компонент для построения SQL запросов для DAO {@link com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao}
 *
 */
@Component
public class RefBookSimpleQueryBuilderComponent {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DBInfo dbInfo;

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
     *
     * @param tableName название таблицы для которой формируется запрос
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор версии записи справочника (фактически поле ID). Используется только при получении всех версий записи
     * @param version дата актуальности, по которой определяется период актуальности и соответственно версия записи, которая в нем действует
     *                Если = null, значит будет выполняться получение всех версий записи
     *                Иначе выполняется получение всех записей справочника, активных на указанную дату
     * @param sortAttribute
     * @param filter
     * @param pagingParams
     * @param isSortAscending
     * @param whereClause

     * @return
     */
    public PreparedStatementData getChildrenQuery(String tableName, RefBook refBook, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
                                                   String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause) {
        PreparedStatementData ps = new PreparedStatementData();

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, simpleFilterTreeListener);

        ps.appendQuery("WITH t AS ");
        ps.appendQuery("(SELECT ");
        // использую два запроса для случаев uniqueRecordId равен null и не равен null (из-за разницы во времени выполнения)
        if (uniqueRecordId != null) {
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
        ps.appendQuery(tableName);
        ps.appendQuery(" frb ");
        if (filterPS.getJoinPartsOfQuery() != null) {
            ps.appendQuery(filterPS.getJoinPartsOfQuery());
        }

        if (version != null) {
            ps.appendQuery(String.format(" WHERE frb.status = 0 and frb.version <= ? and " +
                    "not exists (select 1 from %s r2 where r2.record_id=frb.record_id and r2.status != -1 and r2.version between frb.version + interval '1' day and ?)\n", tableName));
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
        if (uniqueRecordId != null) {
            ps.appendQuery(" START WITH frb." + (uniqueRecordId == null ? "PARENT_ID is null" : "PARENT_ID = " + uniqueRecordId));
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
            fields.append(", \"record_version_from\", (SELECT MIN(VERSION) FROM " + tableName + " rbo1 where rbo1.VERSION>\"record_version_from\") \"record_version_to\" ");
        }
        if (uniqueRecordId != null) {
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
            ps.appendQuery(tableName);
            ps.appendQuery(" frb ");

            ps.appendQuery("START WITH frb.id IN (SELECT id FROM t) \n");
            ps.appendQuery(" CONNECT BY PRIOR PARENT_ID = ID\n");
        }
        ps.appendQuery(")");

        ps.appendQuery("SELECT * FROM (");
        ps.appendQuery("SELECT ");
        ps.appendQuery(fields.toString());
        if (uniqueRecordId != null) {
            ps.appendQuery(", " + RefBook.RECORD_HAS_CHILD_ALIAS);
        } else {
            ps.appendQuery(", CASE WHEN EXISTS(SELECT 1 FROM res res1 WHERE res.record_id = res1.parent_id) THEN 1 ELSE 0 END as " + RefBook.RECORD_HAS_CHILD_ALIAS);
        }
        if (isSupportOver() && sortAttribute != null) {
            ps.appendQuery(",");
            ps.appendQuery(" row_number()");
            // Надо делать сортировку
            ps.appendQuery(" over (order by ");
            ps.appendQuery(sortAttribute.getAlias());
            ps.appendQuery(isSortAscending ? " ASC" : " DESC");
            ps.appendQuery(")");
            ps.appendQuery(" as row_number_over\n");
        } else {
            // База тестовая и не поддерживает row_number() значит сортировка работать не будет
            ps.appendQuery(", rownum row_number_over\n");
        }
        ps.appendQuery(" FROM res\n");
        if (uniqueRecordId == null) {
            ps.appendQuery("WHERE ");
            ps.appendQuery("PARENT_ID is null");
        }
        ps.appendQuery(")");

        if (pagingParams != null) {
            ps.appendQuery(" WHERE row_number_over BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
        }
        return ps;
    }

    /**
     * Формирует простой sql-запрос по выборке данных учитывая иерархичность таблицы
     *
     * @param refBook         справочник
     * @param sortAttribute
     * @param filter
     * @param pagingParams
     * @param isSortAscending
     * @return
     */
    //TODO вместо PARENT_ID использовать com.aplana.sbrf.taxaccounting.model.refbook.RefBook.RECORD_PARENT_ID_ALIAS (Marat Fayzullin 26.03.2014)
    public PreparedStatementData getChildRecordsQuery(RefBook refBook, Long parentId, Date version, RefBookAttribute sortAttribute,
                                                       String filter, PagingParams pagingParams, boolean isSortAscending) {
        String orderBy = "";
        PreparedStatementData ps = new PreparedStatementData();

        ps.appendQuery("WITH tc AS (SELECT level as lvl, ");
        ps.appendQuery(" CONNECT_BY_ROOT frb.ID as \"RECORD_ID\" ");
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", CONNECT_BY_ROOT frb.");
            ps.appendQuery(attribute.getAlias());
            ps.appendQuery(" as \"");
            ps.appendQuery(attribute.getAlias());
            ps.appendQuery("\" ");
        }

        ps.appendQuery(" FROM ");
        ps.appendQuery(refBook.getTableName());
        ps.appendQuery(" frb ");

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getJoinPartsOfQuery() != null){
            ps.appendQuery(filterPS.getJoinPartsOfQuery());
        }

        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE ");
            if (parentId == null) {
                ps.appendQuery("frb.PARENT_ID is null or (");
            }
            ps.appendQuery(filterPS.getQuery().toString());
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
            if (parentId == null) {
                ps.appendQuery(")");
            }
        }

        ps.appendQuery(" CONNECT BY NOCYCLE PRIOR frb.id = frb.PARENT_ID ");
        ps.appendQuery(" START WITH ");
        ps.appendQuery(parentId == null ? " frb.PARENT_ID is null " : " frb.PARENT_ID = " + parentId);

        ps.appendQuery(")\n");

        ps.appendQuery("SELECT res.*, ");
        appendSortClause(ps, refBook, sortAttribute, isSortAscending, "");
        ps.appendQuery(" FROM (");
        ps.appendQuery("SELECT DISTINCT ");
        ps.appendQuery("RECORD_ID ");

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }
        ps.appendQuery(", (SELECT 1 FROM dual WHERE EXISTS (SELECT 1 FROM tc tc2 WHERE lvl > 1 AND tc2.record_id = tc.record_id)) AS " + RefBook.RECORD_HAS_CHILD_ALIAS);
        ps.appendQuery(" FROM tc ");
        ps.appendQuery(") res ");


        if (pagingParams != null) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
            ps.appendQuery(" BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
        }

        ps.appendQuery(orderBy);
        return ps;
    }

    /**
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица
     * поддерживает версионирование
     *
     * @param refBook         справочник
     * @param version   дата актуальности, по которой определяется период актуальности и соответственно версия записи,
     *                        которая в нем действует
     *                        Если = null, значит будет выполняться получение всех версий записи
     *                        Иначе выполняется получение всех записей справочника, активных на указанную дату
     * @param sortAttribute
     * @param filter
     * @param pagingParams
     * @param isSortAscending
     * @return
     */
    public PreparedStatementData getSimpleQuery(RefBook refBook, Date version, RefBookAttribute sortAttribute,
                                                String filter, PagingParams pagingParams, boolean isSortAscending, boolean onlyId) {
        String orderBy = "";
        PreparedStatementData ps = new PreparedStatementData();
        if (onlyId) {
            ps.appendQuery("SELECT ");
            ps.appendQuery(RefBook.RECORD_ID_ALIAS);

        } else {
            ps.appendQuery("SELECT row_number_over, ");
            ps.appendQuery("id ");
            ps.appendQuery(RefBook.RECORD_ID_ALIAS);
            for (RefBookAttribute attribute : refBook.getAttributes()) {
                ps.appendQuery(", ");
                ps.appendQuery(attribute.getAlias());
            }
        }
        ps.appendQuery(" FROM (SELECT ");
        appendSortClause(ps, refBook, sortAttribute, isSortAscending, "frb.");
        ps.appendQuery(", frb.* FROM ");
        ps.appendQuery(refBook.getTableName());
        ps.appendQuery(" frb ");

        if (version != null && refBook.isVersioned()) {
            ps.appendQuery("WHERE version <= ?");
            ps.addParam(version);
        }

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getJoinPartsOfQuery() != null){
            ps.appendQuery(filterPS.getJoinPartsOfQuery());
        }
        if (filterPS.getQuery().length() > 0) {
            if (version == null) {
                ps.appendQuery(" WHERE ");
            } else {
                ps.appendQuery(" AND ");
            }
            ps.appendQuery(filterPS.getQuery().toString());
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
        }

        ps.appendQuery(")");
        if (pagingParams != null) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
            ps.appendQuery(" BETWEEN ? AND ?");
            int startIndex = pagingParams.getStartIndex() == 0 ? 1 : pagingParams.getStartIndex();
            ps.addParam(startIndex);
            ps.addParam(startIndex + pagingParams.getCount() - 1);
        }

        ps.appendQuery(orderBy);
        return ps;
    }

    private void appendSortClause(PreparedStatementData ps, RefBook refBook, RefBookAttribute sortAttribute, boolean isSortAscending, String prefix) {
        RefBookAttribute defaultSort = refBook.getSortAttribute();
        String sortAlias = sortAttribute == null ? (defaultSort == null ? "id" : defaultSort.getAlias()) : sortAttribute.getAlias();
        if (isSupportOver()) {
            // row_number() over (order by ... asc\desc)
            ps.appendQuery("row_number() over ( order by ");
            ps.appendQuery(prefix);
            ps.appendQuery(sortAlias);
            ps.appendQuery(isSortAscending ? " ASC)" : " DESC)");
        } else {
            ps.appendQuery("rownum");
        }
        ps.appendQuery(" as ");
        ps.appendQuery(RefBook.RECORD_SORT_ALIAS);
    }

    private boolean isSupportOver() {
        return dbInfo.isSupportOver();
    }
}
