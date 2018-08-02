package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components;

import com.aplana.sbrf.taxaccounting.dao.impl.DBInfo;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.QueryBuilder;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * Компонент для построения SQL запросов для DAO {@link com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookSimpleDaoImpl}
 */
@Component
public class RefBookSimpleQueryBuilderComponent {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DBInfo dbInfo;
    @Autowired
    private RefBookDao refbookDao;

    /**
     * @param refBook         справочник
     * @param parentId        уникальный идентификатор версии записи справочника (фактически поле ID). Используется только при получении всех версий записи
     * @param version         дата актуальности, по которой определяется период актуальности и соответственно версия записи, которая в нем действует
     *                        Если = null, значит будет выполняться получение всех версий записи
     *                        Иначе выполняется получение всех записей справочника, активных на указанную дату
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
        for (RefBookAttribute attribute : getNotSystemAttributes(refBook.getAttributes())) {
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

            for (RefBookAttribute attribute : getNotSystemAttributes(refBook.getAttributes())) {
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

    private static final String WITH_VERSION_STATEMENT =
            "with t as (select max(version) version, record_id from %s r where status = 0 and version <= ?  and\n" +
                    "not exists (select 1 from %s r2 where r2.record_id=r.record_id and r2.status != -1 and r2.version between r.version + interval '1' day and ?)\n" +
                    "group by record_id),\n" +
                    "minNextVersion as (select r.record_id, min(r.version) version from %s r, t cv where r.version > cv.version and r.record_id= cv.record_id and r.status != -1 group by r.record_id),\n" +
                    "nextVersionEnd as (select mnv.record_id, mnv.version, r.status from minNextVersion mnv, %s r where mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n";

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
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица. Учитывает поиск/фильтрацию по определенным полям сущностей
     *
     * @param refBook         справочник
     * @param filter          параметры фильтрации
     * @param pagingParams    параметры пагинации
     * @param isSortAscending порядок сортировки
     * @return
     */
    public PreparedStatementData psGetRecordsQueryWithFilter(RefBook refBook, List<String> columns, String filter, PagingParams pagingParams, boolean isSortAscending) {
        PreparedStatementData ps = new PreparedStatementData();
        if (refBook.isVersioned()) {
            ps.appendQuery(String.format(sqlRecordVersionsAll(), refBook.getTableName()));
            ps.addParam(VersionedObjectStatus.NORMAL.getId());
        }
        return psGetRecordsQuery(refBook, ps, false, null, columns, filter, pagingParams, isSortAscending, false, false);
    }

    /**
     * Получает название столбца в таблице БД по которому надо отсортировать записи. Для ссылочных атрибутов выполняется разыменование
     *
     * @param sortAttribute атрибут, по которому надо отсортировтать записи
     * @return название столбца в таблице БД
     */
    private String getSortColumnName(RefBookAttribute sortAttribute) {
        String sortColumn;
        if (sortAttribute != null) {
            if (sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                sortColumn = "a_sort";
            } else {
                sortColumn = sortAttribute.getAlias();
            }
        } else {
            // По умолчанию сортируем записи по ID
            sortColumn = "id";
        }
        return sortColumn;
    }

    /**
     * Выполняет формирование JOIN-части запроса
     *
     * @param refBook       справочник
     * @param filter        SQL-запрос с дополнительной фильтрацией записей
     * @param sortAttribute атрибут по которому надо отсортировать записи. Включается в запрос, если является ссылочным
     * @return JOIN-часть SQL-запроса
     */
    private String getJoinPart(RefBook refBook, String filter, RefBookAttribute sortAttribute) {
        StringBuilder joinPart = new StringBuilder();
        if (sortAttribute != null && sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
            // Если атрибут ссылочный - надо добавить join на его таблицу для правильной сортировки по ссылочному столбцу
            RefBook sortedRefBookLink = refbookDao.get(sortAttribute.getRefBookId());
            joinPart.append(String.format("  LEFT JOIN %s a_sort ON a_sort.id = frb.%s \n", sortedRefBookLink.getTableName(), sortAttribute.getAlias()));
        }
        if (StringUtils.isNotEmpty(filter)) {
            // Добавляем join на ссылочные атрибуты, если видимы и отображаются в таблице для фильтрации по ним
            for (RefBookAttribute attribute : refBook.getAttributes()) {
                if (attribute.isVisible() && attribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                    RefBook linkedRefBook = refbookDao.get(attribute.getRefBookId());
                    if (attribute.isRequired()) {
                        joinPart.append("  JOIN ");
                    } else {
                        joinPart.append("  LEFT JOIN ");
                    }
                    joinPart.append(linkedRefBook.getTableName()).append(" ").append(attribute.getAlias())
                            .append(" ON ").append(attribute.getAlias()).append(".ID = ").append("frb.").append(attribute.getAlias()).append("\n");
                }
            }
        }
        return joinPart.toString();
    }

    private String getSelectPart(RefBookAttribute sortAttribute) {
        if (sortAttribute != null && sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
            return ", a_sort." + sortAttribute.getRefBookAttribute().getAlias() + " a_sort";
        }
        return "";
    }

    /**
     * Формирует запрос и параметры для отбора всех записей версионируемого справочника на определенную дату актуальности
     *
     * @param refBook       справочник
     * @param version       дата актуальности
     * @param filter        фильтр для отбора записей. Фактически кусок SQL-запроса
     * @param pagingParams  параметры пэйджинга
     * @param sortAttribute атрибут, по которому будут отсортированы записи
     * @param direction     направление сортировки
     * @return SQL-запрос + параметры
     */
    public QueryBuilder allRecordsByVersion(RefBook refBook, @NotNull Date version, String filter, PagingParams pagingParams,
                                            RefBookAttribute sortAttribute, String direction) {
        QueryBuilder q = new QueryBuilder();

        q.append("SELECT p.*, ").append("p.version as ").append(RefBook.RECORD_VERSION_FROM_ALIAS)
                .append(", (SELECT min(version) - interval '1' day FROM ").append(refBook.getTableName())
                .append(" WHERE status in (0,2) and record_id = p.record_id and version > p.version) as ").append(RefBook.RECORD_VERSION_TO_ALIAS).append(" \n")
                .append("FROM ( \n")
                .append(" SELECT frb.*").append(getSelectPart(sortAttribute)).append(" FROM ").append(refBook.getTableName()).append(" frb\n")
                .append(getJoinPart(refBook, filter, sortAttribute))
                .append(" WHERE frb.status = 0 and (:version is null or frb.version = (select max(version) FROM ")
                .append(refBook.getTableName())
                .append(" WHERE version <= :version and record_id = frb.record_id))\n")
                .append(StringUtils.isNotEmpty(filter) ? " AND (" + filter + ")\n" : "")
                .append(" ) p\n");
        q.addNamedParam("version", version);

        return q.withSort(getSortColumnName(sortAttribute), direction)
                .withPaging(pagingParams);
    }

    private String getColumnFilterQuery(List<String> columns, String searchPattern, String filter) {
        StringBuilder q = new StringBuilder();
        if (!CollectionUtils.isEmpty(columns)) {
            if (StringUtils.isNotEmpty(filter)) {
                q.append("(")
                        .append(filter)
                        .append(") ");
            }
            if (StringUtils.isNotEmpty(searchPattern)) {
                if (StringUtils.isNotEmpty(filter)) {
                    q.append("and ");
                }
                q.append("(");
                for (Iterator<String> it = columns.iterator(); it.hasNext(); ) {
                    String column = it.next();
                    q.append("lower(frb.").append(column).append(") like '%").append(searchPattern.toLowerCase()).append("%'");
                    if (it.hasNext()) {
                        q.append(" or ");
                    }
                }
                q.append(") ");
            }
        }
        return q.toString().trim();
    }

    /**
     * Формирует запрос и параметры для отбора всех записей версионируемого справочника на определенную дату актуальности с учетом поиска по конкретным столбцам
     * Фильтрация по ссылочным столбцам не реализована
     *
     * @param refBook       справочник
     * @param version       дата актуальности
     * @param columns       список стобцов в БД, по которым будет выполнена фильтрация
     * @param searchPattern строка с фильтрацией по столбцам. НЕ SQL-запрос, просто текст с которым будут сраваниваться значения колонок
     * @param pagingParams  параметры пэйджинга
     * @param sortAttribute атрибут, по которому будут отсортированы записи
     * @param direction     направление сортировки
     * @return SQL-запрос + параметры
     */
    public QueryBuilder allRecordsByVersion(RefBook refBook, @NotNull Date version, List<String> columns, String searchPattern, String filter, PagingParams pagingParams,
                                            RefBookAttribute sortAttribute, String direction) {
        QueryBuilder q = new QueryBuilder();

        q.append("SELECT p.*, ").append("p.version as ").append(RefBook.RECORD_VERSION_FROM_ALIAS)
                .append(", (SELECT min(version) - interval '1' day FROM ").append(refBook.getTableName())
                .append(" WHERE status in (0,2) and record_id = p.record_id and version > p.version) as ").append(RefBook.RECORD_VERSION_TO_ALIAS).append(" \n")
                .append("FROM ( \n")
                .append(" SELECT frb.*").append(getSelectPart(sortAttribute)).append(" FROM ").append(refBook.getTableName()).append(" frb\n")
                .append(getJoinPart(refBook, null, sortAttribute))
                .append(" WHERE frb.status = 0 and (:version is null or frb.version = (select max(version) FROM ")
                .append(refBook.getTableName())
                .append(" WHERE version <= :version and record_id = frb.record_id))\n")
                .append(!CollectionUtils.isEmpty(columns) && (StringUtils.isNotEmpty(searchPattern) || StringUtils.isNotEmpty(filter)) ? " AND " + getColumnFilterQuery(columns, searchPattern, filter) + "\n" : "")
                .append(" ) p\n");
        q.addNamedParam("version", version);

        return q.withSort(getSortColumnName(sortAttribute), direction)
                .withPaging(pagingParams);
    }

    /**
     * Формирует запрос и параметры для отбора всех записей неверсионируемого справочника с учетом поиска по конкретным столбцам
     * Фильтрация по ссылочным столбцам не реализована
     *
     * @param refBook       справочник
     * @param columns       список стобцов в БД, по которым будет выполнена фильтрация
     * @param searchPattern строка с шаблоном поиска по столбцам. НЕ SQL-запрос, просто текст с которым будут сраваниваться значения колонок
     * @param filter        часть sql запроса для дополнительной фильтрации
     * @param pagingParams  параметры пэйджинга
     * @param sortAttribute атрибут, по которому будут отсортированы записи
     * @param direction     направление сортировки
     * @return SQL-запрос + параметры
     */
    public QueryBuilder allRecords(RefBook refBook, List<String> columns, String searchPattern, String filter, PagingParams pagingParams,
                                   RefBookAttribute sortAttribute, String direction) {
        QueryBuilder q = new QueryBuilder();

        q.append("SELECT frb.*").append(getSelectPart(sortAttribute)).append(" FROM ").append(refBook.getTableName()).append(" frb\n")
                .append(getJoinPart(refBook, null, sortAttribute));

        if (!refBook.isReadOnly()) {
            q.append(" WHERE frb.status = 0");
        }

        // Добавляем фильтрацию по выбранным столбцам
        if (!CollectionUtils.isEmpty(columns) && (StringUtils.isNotEmpty(searchPattern) || StringUtils.isNotEmpty(filter))) {
            q.append(refBook.isReadOnly() ? " WHERE " : " AND ").append(getColumnFilterQuery(columns, searchPattern, filter));
        }

        return q.withSort(getSortColumnName(sortAttribute), direction)
                .withPaging(pagingParams);
    }

    /**
     * Формирует запрос и параметры для отбора всех записей неверсионируемого справочника
     *
     * @param refBook       справочник
     * @param filter        фильтр для отбора записей. Фактически кусок SQL-запроса
     * @param pagingParams  параметры пэйджинга
     * @param sortAttribute атрибут, по которому будут отсортированы записи
     * @param direction     направление сортировки
     * @return SQL-запрос + параметры
     */
    public QueryBuilder allRecords(RefBook refBook, String filter, PagingParams pagingParams,
                                   RefBookAttribute sortAttribute, String direction) {
        QueryBuilder q = new QueryBuilder();

        q.append("SELECT frb.*").append(getSelectPart(sortAttribute)).append(" FROM ").append(refBook.getTableName()).append(" frb\n")
                .append(getJoinPart(refBook, filter, sortAttribute));

        if (!refBook.isReadOnly()) {
            q.append(" WHERE frb.status = 0");
        }
        if (StringUtils.isNotEmpty(filter)) {
            q.append((refBook.isReadOnly() ? " WHERE " : " AND (") + filter);
            q.append((refBook.isReadOnly() ? "\n" : ")\n"));
        }

        return q.withSort(getSortColumnName(sortAttribute), direction)
                .withPaging(pagingParams);
    }

    /**
     * TODO метод реализован неоптимально, но оставлен для совместимости, вместо него надо использовать RefBookSimpleQueryBuilderComponent#allRecordsByVersion
     *
     * @return
     */
    @Deprecated
    public PreparedStatementData psGetRecordsQuery(RefBook refBook, Long recordId, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
                                                   String filter, PagingParams pagingParams, boolean isSortAscending, boolean onlyId, boolean withVersionInfo) {
        PreparedStatementData ps = new PreparedStatementData();
        if (refBook.isVersioned()) {
            if (version != null) {
                if (withVersionInfo) {
                    ps.appendQuery(String.format(WITH_VERSION_STATEMENT, refBook.getTableName(), refBook.getTableName(),
                            refBook.getTableName(), refBook.getTableName()));
                } else {
                    ps.appendQuery(String.format(WITH_STATEMENT, refBook.getTableName(), refBook.getTableName()));
                }
                ps.addParam(version);
                ps.addParam(version);
                return psGetRecordsQuery(refBook, ps, true, sortAttribute, null, filter, pagingParams, isSortAscending, onlyId, withVersionInfo);
            } else {
                if (uniqueRecordId != null) {
                    ps.appendQuery(String.format(sqlRecordVersions(), refBook.getTableName(), refBook.getTableName()));
                    ps.addParam(uniqueRecordId);
                    ps.addParam(VersionedObjectStatus.NORMAL.getId());
                } else if (recordId != null) {
                    ps.appendQuery(String.format(sqlRecordVersionsByRecordId(), refBook.getTableName(), recordId));
                    ps.addParam(VersionedObjectStatus.NORMAL.getId());
                } else {
                    ps.appendQuery(String.format(sqlRecordVersionsAll(), refBook.getTableName()));
                    ps.addParam(VersionedObjectStatus.NORMAL.getId());
                }
                return psGetRecordsQuery(refBook, ps, false, sortAttribute, null, filter, pagingParams, isSortAscending, onlyId, withVersionInfo);
            }
        } else {
            return psGetRecordsQuery(refBook, ps, false, sortAttribute, null, filter, pagingParams, isSortAscending, onlyId, false);
        }
    }

	/*
     Пример запроса:

     SELECT * FROM (SELECT r.id, r.record_id, r.version, COALESCE(
                  (SELECT MIN(VERSION) FROM get_records_test w
                   WHERE w.version > r.version AND w.record_id = r.record_id
                   AND w.status IN (0, 2)), to_date('31.12.2099', 'dd.mm.yyyy')) end_version,
                   r.a, r.b
     FROM get_records_test r
     WHERE r.status = 0 AND r.version <= TO_DATE('01.09.2016', 'dd.mm.yyyy'))
     WHERE end_version > TO_DATE('01.06.2016', 'dd.mm.yyyy')
	 */

    /**
     * Формирует запрос для выборки версии записей справочника за интервал времени
     *
     * @param refBook     справочник, для которого формируется запрос
     * @param versionFrom начала периода
     * @param versionTo   конец периода
     * @param filter      условия фильтрации (<strong>пока не реализовано</strong>)
     * @return
     */
    @Deprecated
    public PreparedStatementData psGetRecordsQuery(RefBook refBook, Date versionFrom, Date versionTo, String filter) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("SELECT * FROM (SELECT r.id ")
                .appendQuery(RefBook.RECORD_ID_ALIAS).appendQuery(", r.record_id ")
                .appendQuery(RefBook.BUSINESS_ID_ALIAS).appendQuery(", r.version ")
                .appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS).appendQuery(", COALESCE( ")
                .appendQuery("(SELECT MIN(version) FROM ").appendQuery(refBook.getTableName())
                .appendQuery(" w WHERE w.version > r.version AND w.record_id = r.record_id ")
                .appendQuery("AND w.status IN (0, 2)), :maxDate) ").appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
        // добавляем атрибуты
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (!attribute.getAlias().equalsIgnoreCase(RefBook.RECORD_ID_ALIAS) &&
                    !attribute.getAlias().equalsIgnoreCase(RefBook.BUSINESS_ID_ALIAS) &&
                    !attribute.getAlias().equalsIgnoreCase(RefBook.RECORD_VERSION_FROM_ALIAS) &&
                    !attribute.getAlias().equalsIgnoreCase(RefBook.RECORD_VERSION_TO_ALIAS)) {
                ps.appendQuery(", r.");
                ps.appendQuery(attribute.getAlias());
            }
        }
        ps.appendQuery(" FROM ").appendQuery(refBook.getTableName()).appendQuery(" r WHERE r.status = 0 AND r.version <= :versionTo) ")
                .appendQuery("WHERE ").appendQuery(RefBook.RECORD_VERSION_TO_ALIAS).appendQuery(" > :versionFrom");

        ps.addNamedParam("versionFrom", versionFrom);
        ps.addNamedParam("versionTo", versionTo);
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(2099, 11, 31);
        ps.addNamedParam("maxDate", maxDate.getTime());

		/*PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
		simpleFilterTreeListener.setRefBook(refBook);
		simpleFilterTreeListener.setPs(filterPS);

		Filter.getFilterQuery(filter, simpleFilterTreeListener);*/
        return ps;
    }

    @Deprecated
    public PreparedStatementData psGetRecordsQuery(RefBook refBook, PreparedStatementData ps, boolean checkVersion, RefBookAttribute sortAttribute, List<String> columns,
                                                   String filter, PagingParams pagingParams, boolean isSortAscending, boolean onlyId, boolean withVersion) {
        ps.appendQuery("SELECT /*+ FIRST_ROWS */* FROM (");
        if (onlyId) {
            ps.appendQuery("SELECT ")
                    .appendQuery(RefBook.RECORD_ID_ALIAS)
                    .appendQuery(" FROM ");
        } else {
            ps.appendQuery("SELECT res.*, rownum row_number_over FROM ");
        }

        ps.appendQuery("(SELECT frb.id AS ").appendQuery(RefBook.RECORD_ID_ALIAS);
        if (refBook.isVersioned()) {
            if (!checkVersion) {
                ps.appendQuery(",  t.version AS ");
                ps.appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS);
                ps.appendQuery(",");

                ps.appendQuery("  t.versionEnd AS ");
                ps.appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
            } else if (withVersion) {
                ps.appendQuery(", t.version AS ").appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS)
                        .appendQuery(", nve.version - interval '1' day AS ").appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
            }

            if (refBook.getId() != RefBook.Id.DEPARTMENT.getId()) {
                ps.appendQuery(", t.record_id AS ").appendQuery(RefBook.BUSINESS_ID_ALIAS);
            }
        }

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (!attribute.getAlias().equalsIgnoreCase(RefBook.RECORD_ID_ALIAS) && !attribute.getAlias().equalsIgnoreCase(RefBook.BUSINESS_ID_ALIAS)) {
                ps.appendQuery(", frb.");
                ps.appendQuery(attribute.getAlias());
            }
        }
        ps.appendQuery(" FROM ");
        if (refBook.isVersioned()) {
            ps.appendQuery(" t, ");
        }
        ps.appendQuery(refBook.getTableName())
                .appendQuery(" frb \n");
        if (refBook.isVersioned() && withVersion) {
            ps.appendQuery("left join nextVersionEnd nve on nve.record_id = frb.record_id \n");
        }
        if (sortAttribute != null && sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
            RefBook sortedRefBookLink = refbookDao.get(sortAttribute.getRefBookId());
            ps.appendQuery(String.format("left join %s a_sort on a_sort.id = frb.%s \n", sortedRefBookLink.getTableName(), sortAttribute.getAlias()));
        }

        PreparedStatementData filterPS = new PreparedStatementData();
        if (!CollectionUtils.isEmpty(columns) && StringUtils.isNotEmpty(filter)) {
            ps.appendQuery(" WHERE (");
            for (Iterator<String> it = columns.iterator(); it.hasNext(); ) {
                String column = it.next();
                ps.appendQuery("lower(frb.").appendQuery(column).appendQuery(") like '%").appendQuery(filter.toLowerCase()).appendQuery("%'");
                if (it.hasNext()) {
                    ps.appendQuery(" or ");
                }
            }
            ps.appendQuery(") ");
        } else if (StringUtils.isNotEmpty(filter)) {
            SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
            simpleFilterTreeListener.setRefBook(refBook);
            simpleFilterTreeListener.setPs(filterPS);

            Filter.getFilterQuery(filter, simpleFilterTreeListener);
            if (filterPS.getJoinPartsOfQuery() != null) {
                ps.appendQuery(filterPS.getJoinPartsOfQuery());
            }
            if (filterPS.getQuery().length() > 0) {
                ps.appendQuery(" WHERE (")
                        .appendQuery(filterPS.getQuery().toString());
                if (!filterPS.getParams().isEmpty()) {
                    ps.addParam(filterPS.getParams());
                }
                ps.appendQuery(") ");
            }
        }
        if (refBook.isVersioned()) {
            if (StringUtils.isEmpty(filter) && StringUtils.isEmpty(filterPS.getQuery())) {
                ps.appendQuery(" WHERE ");
            } else {
                ps.appendQuery(" AND ");
            }
            ps.appendQuery("(frb.version = t.version AND frb.record_id = t.record_id AND frb.status = 0)");
        }

        if (sortAttribute != null) {
            String tableAlias = "frb";
            if (sortAttribute.getAttributeType() == RefBookAttributeType.REFERENCE) {
                sortAttribute = sortAttribute.getRefBookAttribute();
                tableAlias = "a_sort";
            }
            ps.appendQuery(" ORDER BY ")
                    .appendQuery(tableAlias + "." + sortAttribute.getAlias())
                    .appendQuery(isSortAscending ? " ASC" : " DESC");
        } else {
            ps.appendQuery(" ORDER BY frb.id");
        }
        if (refBook.isVersioned() && !checkVersion) {
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
        //TODO !!! неправильно, так как групп уникальности может быть несколько
        List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues = groupsUniqueAttributesValues.get(1);

        PreparedStatementData sql = new PreparedStatementData("SELECT r.").append(RefBook.RECORD_ID_ALIAS).append(" AS id, ");
        appendNameColumn(sql, uniqueAttributesValues);
        sql.append("FROM ").append(refBook.getTableName()).append(" r\n");
        sql.append("WHERE r.status = 0\nAND (\n");
        appendWhereCondition(record, sql, uniqueAttributesValues);
        sql.append(")");
        if (uniqueRecordId != null) {
            sql.append("\nAND r.").append(RefBook.RECORD_ID_ALIAS).append(" != ").append(uniqueRecordId);
        }
        if (record.getRecordId() != null) {
            sql.append("\nAND r.record_id != ").append(record.getRecordId());
        }
        return sql;
    }

    private void appendNameColumn(PreparedStatementData sql, List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues) {
        for (int j = 0; j < uniqueAttributesValues.size(); j++) {
            RefBookAttribute attribute = uniqueAttributesValues.get(j).getFirst();

            sql.append("'").append(attribute.getName()).append("'");
            appendIfAttributeIsNotLast(sql, uniqueAttributesValues, j, "||', '||");
        }
        sql.append(" AS name\n");
    }

    private void appendWhereCondition(RefBookRecord record, PreparedStatementData sql,
                                      List<Pair<RefBookAttribute, RefBookValue>> uniqueAttributesValues) {
        for (int j = 0; j < uniqueAttributesValues.size(); j++) {
            RefBookAttribute attribute = uniqueAttributesValues.get(j).getFirst();
            sql.addNamedParam(attribute.getAlias(), record.getValues().get(attribute.getAlias()).getValue());
            sql.append("(r.").append(attribute.getAlias()).append(" = :").append(attribute.getAlias());
            if (!attribute.isRequired()) {
                sql.append(" OR (r.").append(attribute.getAlias()).append(" is NULL AND :")
                        .append(attribute.getAlias()).append(" is NULL)");
            }
            sql.append(") ");
            appendIfAttributeIsNotLast(sql, uniqueAttributesValues, j, "AND ");
        }
    }

    private void appendIfAttributeIsNotLast(PreparedStatementData sql, List list, int index, String query) {
        if (index < list.size() - 1) {
            sql.append(query);
        }
    }

    private static final String CHECK_CONFLICT_VALUES_VERSIONS =
            "with conflictRecord as (select id, record_id from %1$s where %2$s),\n" +
                    "allRecordsInConflictGroup AS (SELECT distinct r.id, r.record_id, r.version " +
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


    public PreparedStatementData psCheckConflictValuesVersions(RefBook refBook, List<Long> recordIds, Date versionFrom, Date versionTo) {
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

    public PreparedStatementData psCheckParentConflict(RefBook refBook, Long parentId, Date versionFrom, Date versionTo) {
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

    public PreparedStatementData psCheckCrossVersions(RefBook refBook) {
        String partition = isSupportOver() ? CHECK_CROSS_VERSIONS_PARTITION : "";
        return new PreparedStatementData(String.format(CHECK_CROSS_VERSIONS, refBook.getTableName(), partition));
    }

    private static final String IS_VERSION_USED_LIKE_PARENT =
            "select r.version as version, \n" +
                    "  (SELECT min(version) - interval '1' DAY FROM %1$s rn WHERE rn.record_id = r.record_id AND rn.version > r.version) AS versionEnd\n" +
                    "from %1$s r where r.%2$s = :parentId and r.version >= :versionFrom and r.status != -1";

    public PreparedStatementData psVersionUsedLikeParent(RefBook refBook) {
        String query = String.format(IS_VERSION_USED_LIKE_PARENT, refBook.getTableName(), RefBook.RECORD_PARENT_ID_ALIAS);
        return new PreparedStatementData(query);
    }

    public PreparedStatementData psGetNextVersion(RefBook refBook, Date version, String filter) {

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);
        Filter.getFilterQuery(filter, simpleFilterTreeListener);

        PreparedStatementData sql = new PreparedStatementData();
        sql.appendQuery("SELECT MIN(frb.version) AS version\n");
        sql.appendQuery(getFromStatementForGetNextVersion(refBook, filterPS).toString());
        sql.appendQuery("WHERE\n frb.version > :version\n AND status <> -1\n");
        sql.addNamedParam("version", version);

        if (filterPS.getQuery().length() > 0) {
            sql.appendQuery(" AND\n (");
            sql.appendQuery(filterPS.getQuery().toString());
            sql.appendQuery(")\n");
        }
        return sql;
    }

    private StringBuilder getFromStatementForGetNextVersion(RefBook refBook, PreparedStatementData filterPS) {
        StringBuilder fromSql = new StringBuilder();
        fromSql.append("FROM ");
        fromSql.append(refBook.getTableName());
        fromSql.append(" frb\n");

        if (filterPS.getJoinPartsOfQuery() != null) {
            fromSql.append(filterPS.getJoinPartsOfQuery());
        }
        return fromSql;
    }

    /*
    INSERT INTO <ref_book_table_name> (id, record_id, version, status, <attribute1>, <attributeN>)
    VALUES (seq_ref_book_record.nextval, <attribute1_value>, <attributeN_value>)
     */
    public PreparedStatementData psCreateFakeRecordVersion(RefBook refBook, Long recordId, Date version) {
        PreparedStatementData sql = new PreparedStatementData();
        List<RefBookAttribute> allRequiredAttributes = getRequiredAttributesListFromBook(refBook);
        List<RefBookAttribute> requiredAttributes = getNotSystemAttributes(allRequiredAttributes);

        sql.addNamedParam("recordId", recordId);
        sql.addNamedParam("version", new java.sql.Date(version.getTime()));

        sql.append("INSERT INTO ").append(refBook.getTableName()).append(" (id, record_id, version, status");
        for (RefBookAttribute requiredAttribute : requiredAttributes) {
            sql.append(", ").append(requiredAttribute.getAlias());
        }
        sql.append(")\nVALUES (seq_ref_book_record.nextval, :recordId, :version, 2");
        for (RefBookAttribute requiredAttribute : requiredAttributes) {
            sql.append(", ");
            appendFakeAttributeValue(sql, requiredAttribute);
        }
        sql.append(")");
        return sql;
    }

    private List<RefBookAttribute> getNotSystemAttributes(List<RefBookAttribute> attributes) {
        List<RefBookAttribute> result = new ArrayList<RefBookAttribute>();
        for (RefBookAttribute attribute : attributes) {
            if (!RefBook.SYSTEM_ALIASES.contains(attribute.getAlias().toLowerCase())) {
                result.add(attribute);
            }
        }
        return result;
    }

    private void appendFakeAttributeValue(PreparedStatementData sql, RefBookAttribute requiredAttribute) {
        Date fakeDate = new GregorianCalendar(2016, 0, 1).getTime();
        sql.append(":").append(requiredAttribute.getAlias());

        switch (requiredAttribute.getAttributeType()) {
            case STRING:
                sql.addNamedParam(requiredAttribute.getAlias(), " ");
                break;
            case NUMBER:
                sql.addNamedParam(requiredAttribute.getAlias(), 1);
                break;
            case DATE:
                sql.addNamedParam(requiredAttribute.getAlias(), fakeDate);
                break;
            case REFERENCE:
                sql.addNamedParam(requiredAttribute.getAlias(), -1L);
                break;
        }
    }

    private List<RefBookAttribute> getRequiredAttributesListFromBook(RefBook refBook) {
        List<RefBookAttribute> requiredAttributes = new ArrayList<RefBookAttribute>();
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (attribute.isRequired()) {
                requiredAttributes.add(attribute);
            }
        }
        return requiredAttributes;
    }

    /*
    INSERT INTO <ref_book_table_name> (id, record_id, version, status, <attribute1>, <attributeN>)
    VALUES (<id>, <record_id>, <version>, <status>, <attribute1_value>, <attributeN_value>))
     */
    public PreparedStatementData psCreateRecordVersion(final RefBook refBook) {
        PreparedStatementData sql = new PreparedStatementData();
        List<RefBookAttribute> attributes = getNotSystemAttributes(refBook.getAttributes());

        sql.append("INSERT INTO ").append(refBook.getTableName()).append(" (id, record_id, version, status");
        for (RefBookAttribute attribute : attributes) {
            sql.append(", ").append(attribute.getAlias());
        }
        sql.append(")\nVALUES (:id, :recordId, :version, :status");
        for (RefBookAttribute attribute : attributes) {
            sql.append(", :").append(attribute.getAlias());
        }
        sql.append(")");

        return sql;
    }

    /*
    SELECT id <id_alias>, <attribute1>, <attributeN>
    FROM <ref_book_table_name>
    WHERE id = <id>
     */
    public PreparedStatementData psGetRecordData(RefBook refBook) {
        String whereClause = "id = :id";
        return psGetRecordsData(refBook, whereClause);
    }

    /*
    SELECT id <id_alias>, <attribute1>, <attributeN>
    FROM <ref_book_table_name>
    WHERE id in (<id1>,<id2>)
     */
    public PreparedStatementData psGetRecordsData(RefBook refBook, List<Long> recordIds) {
        String inStatement = SqlUtils.transformToSqlInStatement("id", recordIds);
        return psGetRecordsData(refBook, inStatement);
    }

    /**
     * SELECT id <id_alias>, <attribute1>, <attributeN>
     * FROM <ref_book_table_name>
     * WHERE <whereClause>
     */
    public PreparedStatementData psGetRecordsData(RefBook refBook, String whereClause) {
        PreparedStatementData sql = new PreparedStatementData("SELECT id ");
        sql.append(RefBook.RECORD_ID_ALIAS);
        if (refBook.isVersioned() && refBook.getId() != RefBook.Id.DEPARTMENT.getId() && !refBook.hasAttribute(RefBook.BUSINESS_ID_ALIAS)) {
            sql.appendQuery(", ").appendQuery(RefBook.BUSINESS_ID_ALIAS);
        }
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", ").append(attribute.getAlias());
        }
        sql.append("\nFROM ").append(refBook.getTableName()).append("\nWHERE ").append(whereClause);
        return sql;
    }

    /**
     * SELECT id <id_alias>, <attribute1>, <attributeN>
     * FROM <ref_book_table_name>
     * WHERE <whereClause> <version>
     */

    /* Пример сформированного sql-запроса:
	   WITH t AS
	  (SELECT max(VERSION) VERSION, record_id
	   FROM REF_BOOK_PERSON r
	   WHERE status = 0 AND VERSION <= '29.06.2017' AND NOT EXISTS ( SELECT 1 FROM REF_BOOK_PERSON r2
			WHERE r2.record_id=r.record_id AND r2.status != -1 AND r2.version BETWEEN r.version + interval '1' DAY AND '29.06.2017')
	   GROUP BY record_id)
	  SELECT *
	  FROM
	  (SELECT res.*, rownum row_number_over
	   FROM
		 (SELECT frb.id AS id, frb.RECORD_ID, frb.LAST_NAME, frb.FIRST_NAME, frb.MIDDLE_NAME, frb.SEX, frb.INN,
				  frb.INN_FOREIGN, frb.SNILS, frb.TAXPAYER_STATE, frb.BIRTH_DATE, frb.BIRTH_PLACE, frb.CITIZENSHIP, frb.ADDRESS,
				  frb.PENSION, frb.MEDICAL, frb.SOCIAL, frb.EMPLOYEE,frb.SOURCE_ID, frb.OLD_ID
		  FROM t
			 JOIN REF_BOOK_PERSON frb ON (frb.version = t.version AND frb.record_id = t.record_id AND frb.status = 0)
			 JOIN ref_book_person p ON (frb.record_id = p.record_id)
			 JOIN RASCHSV_PERS_SV_STRAH_LIC np ON (np.declaration_data_id = 10094 AND p.id = np.person_id)
			) res)
			ORDER BY id

    Поскольку поиск осуществляется с использованием оператора EXISTS необходимодимо всегда связывать поле подзапроса через ALIAS frb, например:
    AND frb.record_id = p.record_id
     */
    public PreparedStatementData psGetRecordsData(RefBook refBook, String whereClause, Date version) {

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(String.format(WITH_STATEMENT, refBook.getTableName(), refBook.getTableName()));
        ps.addParam(version);
        ps.addParam(version);
        ps.appendQuery("SELECT * FROM (")
                .appendQuery("SELECT res.*, rownum row_number_over FROM ")
                .appendQuery("(SELECT frb.id AS ")
                .appendQuery(RefBook.RECORD_ID_ALIAS);
        if (refBook.isVersioned() && refBook.getId() != RefBook.Id.DEPARTMENT.getId() && !refBook.hasAttribute(RefBook.BUSINESS_ID_ALIAS)) {
            ps.appendQuery(", frb.").appendQuery(RefBook.BUSINESS_ID_ALIAS);
        }
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            if (!attribute.getAlias().equalsIgnoreCase(RefBook.RECORD_ID_ALIAS)) {
                ps.appendQuery(", frb.");
                ps.appendQuery(attribute.getAlias());
            }
        }
        ps.appendQuery(" FROM t JOIN ");
        ps.appendQuery(refBook.getTableName());
        ps.appendQuery(" frb ON (frb.version = t.version AND frb.record_id = t.record_id AND frb.status = 0)");

        if (StringUtils.isNotBlank(whereClause)) {
            ps.appendQuery(whereClause);
        }
        ps.appendQuery(") res) ORDER BY id");
        return ps;
    }


    /*
    UPDATE <ref_book_table_name> SET
    <col> = :<col>,
    <col> = :<col>
    WHERE id = <id>
     */
    public PreparedStatementData psUpdateRecordVersion(RefBook refBook, Long uniqueRecordId, Map<String, RefBookValue> values) {
        List<Pair<String, RefBookValue>> valuesPairs = convertMapToPairsList(values);

        PreparedStatementData sql = new PreparedStatementData("UPDATE ").append(refBook.getTableName()).append(" SET\n");
        for (Pair<String, RefBookValue> valuePair : valuesPairs) {
            String columnName = valuePair.getFirst();
            if (columnName.toLowerCase().equals(RefBook.RECORD_ID_ALIAS) || RefBook.SYSTEM_ALIASES.contains(columnName.toLowerCase())) {
                continue;
            }
            Object columnValue = valuePair.getSecond().getValue();

            sql.append(columnName).append(" = :").append(columnName).append(",\n");
            sql.addNamedParam(columnName, columnValue);
        }
        StringBuilder builder = sql.getQuery();
        builder.delete(builder.lastIndexOf(",\n"), builder.length());
        sql.append("\nWHERE id = :id");
        sql.addNamedParam("id", uniqueRecordId);
        return sql;
    }

    private <K, V> List<Pair<K, V>> convertMapToPairsList(Map<K, V> map) {
        List<Pair<K, V>> pairsList = new ArrayList<Pair<K, V>>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            Pair<K, V> pair = new Pair<K, V>(entry.getKey(), entry.getValue());
            pairsList.add(pair);
        }
        return pairsList;
    }
}
