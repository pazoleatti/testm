package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookOktmoDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 *
 * @author dloshkarev
 */
@Repository
public class RefBookOktmoDaoImpl extends AbstractDao implements RefBookOktmoDao {

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    private BDUtils dbUtils;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(String tableName, Long refBookId, Date version, PagingParams pagingParams, String filter,
                                                              RefBookAttribute sortAttribute, boolean isSortAscending, boolean calcHasChild) {
        RefBook refBook = refBookDao.get(refBookId);
        // получаем страницу с данными
        PreparedStatementData ps = getSimpleQuery(tableName, refBook, null, null, version, sortAttribute, filter, pagingParams, isSortAscending, null, false, calcHasChild);
        List<Map<String, RefBookValue>> records;
        if (calcHasChild) {
            records = refBookDao.getRecordsWithHasChild(ps, refBook);
        } else {
            records = refBookDao.getRecordsData(ps, refBook);
        }
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве всех записей с текущим фильтром
        ps = getSimpleQuery(tableName, refBook, null, null, version, sortAttribute, filter, null, isSortAscending, null, false, false);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    @Override
    public Long getRowNum(String tableName, Long refBookId, Date version, Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);
        PreparedStatementData ps = getSimpleQuery(tableName, refBook, null, null, version, sortAttribute, filter, null, isSortAscending, null, false, false);
        return refBookDao.getRowNum(ps, recordId);
    }

    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(String tableName, Long refBookId, Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);
        // получаем страницу с данными
        PreparedStatementData ps = getChildrenQuery(tableName, refBook, parentRecordId, version, sortAttribute, filter, pagingParams, isSortAscending, null);
        List<Map<String, RefBookValue>> records = refBookDao.getRecordsWithHasChild(ps, refBook);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве всех записей с текущим фильтром
        ps = getChildrenQuery(tableName, refBook, parentRecordId, version, sortAttribute, filter, null, isSortAscending, null);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(String tableName, Long refBookId, Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = new RefBook();
        RefBook oldRefBook = refBookDao.get(refBookId);
        refBook.setId(oldRefBook.getId());
        refBook.setName(oldRefBook.getName());
        refBook.setReadOnly(oldRefBook.isReadOnly());
        refBook.setScriptId(oldRefBook.getScriptId());
        refBook.setType(oldRefBook.getType());
        refBook.setVisible(oldRefBook.isVisible());
        refBook.setAttributes(new ArrayList<RefBookAttribute>());
        refBook.getAttributes().addAll(oldRefBook.getAttributes());
        // получаем страницу с данными
        PreparedStatementData ps = getSimpleQuery(tableName, refBook, null, uniqueRecordId, null, sortAttribute, filter, pagingParams, isSortAscending, null, false, false);

        //Добавляем атрибуты версии, т.к они не хранятся в бд
        refBook.getAttributes().add(RefBook.getVersionFromAttribute());
        refBook.getAttributes().add(RefBook.getVersionToAttribute());
        List<Map<String, RefBookValue>> records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве версий
        result.setTotalCount(getRecordVersionsCount(tableName, uniqueRecordId));
        return result;
    }

    private static final String WITH_STATEMENT =
            "with t as (select max(version) version, record_id from %s r where status = 0 and version <= ?  and\n" +
                    "not exists (select 1 from %s r2 where r2.record_id=r.record_id and r2.status != -1 and r2.version between r.version + interval '1' day and ?)\n" +
                    "group by record_id)\n";

    private static final String RECORD_VERSIONS_STATEMENT =
            "with currentRecord as (select id, record_id, version from %s where id=?),\n" +
                    "recordsByVersion as (select r.ID, r.RECORD_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from %s r, currentRecord cr where r.RECORD_ID=cr.RECORD_ID and r.status != -1), \n" +
                    "t as (select rv.rn as row_number_over, rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";
    private static final String RECORD_VERSIONS_ALL =
            "with recordsByVersion as (select r.ID, r.RECORD_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from %s r where r.status != -1), \n" +
                    "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";
    private static final String RECORD_VERSIONS_STATEMENT_BY_RECORD_ID =
            "with recordsByVersion as (select r.ID, r.RECORD_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from %s r where r.record_id=%d and r.status != -1), \n" +
                    "t as (select rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version - interval '1' day versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";

    /**
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица
     * @param tableName название таблицы для которой формируется запрос
     * @param refBook справочник
     * @param uniqueRecordId уникальный идентификатор версии записи справочника (фактически поле ID). Используется только при получении всех версий записи
     * @param version дата актуальности, по которой определяется период актуальности и соответственно версия записи, которая в нем действует
     *                Если = null, значит будет выполняться получение всех версий записи
     *                Иначе выполняется получение всех записей справочника, активных на указанную дату
     * @param sortAttribute атррибут по которому сортируется выборка
     * @param filter параметры фильтрации
     * @param pagingParams параметры пагинации
     * @param isSortAscending порядок сортировки
     * @param whereClause дополнительные условия запроса
     * @param onlyId флаг указывающий на то что в выборке будет только record_id а не полный список полей
     * @return
     */
    private PreparedStatementData getSimpleQuery(String tableName, RefBook refBook, Long recordId, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
                                                String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause, boolean onlyId, boolean calcHasChild) {
        PreparedStatementData ps = new PreparedStatementData();

        if (version != null) {
            ps.appendQuery(String.format(WITH_STATEMENT, tableName, tableName));
            ps.addParam(version);
            ps.addParam(version);
        } else {
            if (uniqueRecordId != null) {
                //Ищем все версии по уникальному идентификатору
                ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT, tableName, tableName));
                ps.addParam(uniqueRecordId);
                ps.addParam(VersionedObjectStatus.NORMAL.getId());
            } else if (recordId != null){
                //Ищем все версии в группе версий
                ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT_BY_RECORD_ID, tableName, recordId));
                ps.addParam(VersionedObjectStatus.NORMAL.getId());
            } else {
                //Ищем вообще все версии
                ps.appendQuery(String.format(RECORD_VERSIONS_ALL, tableName));
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

        if (calcHasChild) {
            ps.appendQuery(", (SELECT 1 FROM dual WHERE EXISTS(SELECT 1 from t, " + tableName + " frb1 " +
                    "WHERE frb1.PARENT_ID = frb.ID AND (frb.version = t.version and frb.record_id = t.record_id))) as " + RefBook.RECORD_HAS_CHILD_ALIAS);
        }

        ps.appendQuery(" FROM t, ");
        ps.appendQuery(tableName);
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
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            if (filterPS.getQuery().length() > 0) {
                ps.appendQuery(" AND ");
            } else {
                ps.appendQuery(" WHERE ");
            }
            ps.appendQuery(whereClause);
        }

        if (filterPS.getQuery().length() > 0 ||
                (whereClause != null && !whereClause.trim().isEmpty() && filterPS.getQuery().length() == 0)) {
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
            ps.appendQuery(" order by frb.CODE");
        }
        if (version == null) {
            ps.appendQuery(" , t.version\n");
        }

        ps.appendQuery(") res) ");

        if (pagingParams != null) {
            ps.appendQuery(" where row_number_over BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
        }
        return ps;
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
    private PreparedStatementData getChildrenQuery(String tableName, RefBook refBook, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
                                                   String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause) {
        PreparedStatementData ps = new PreparedStatementData();

        ps.appendQuery("with t as (select frb.id \n");
        ps.appendQuery(" FROM ");
        ps.appendQuery(tableName);
        ps.appendQuery(" frb \n");
        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getJoinPartsOfQuery() != null) {
            ps.appendQuery(filterPS.getJoinPartsOfQuery());
        }
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE (");
            ps.appendQuery(filterPS.getQuery().toString());
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
            ps.appendQuery(") AND ");
        } else {
            ps.appendQuery(" WHERE ");
        }

        if (version != null) {
            ps.appendQuery(String.format("frb.status = 0 and frb.version <= ? and " +
                "not exists (select 1 from %s r2 where r2.record_id=frb.record_id and r2.status != -1 and r2.version between frb.version + interval '1' day and ?)\n", tableName));
            ps.addParam(version);
            ps.addParam(version);
        } else {
            ps.appendQuery("frb.status = 0");
        }
        ps.appendQuery(") \n");

        ps.appendQuery(", ct AS ");
        ps.appendQuery("(select distinct ");
        ps.appendQuery("frb.id as \"RECORD_ID\"");

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
        ps.appendQuery(" CONNECT BY PRIOR PARENT_ID = ID )");

        ps.appendQuery("SELECT res.*, (SELECT 1 FROM dual WHERE EXISTS(SELECT 1 from ct \n" +
                " WHERE ct.PARENT_ID = res.record_id)) as " + RefBook.RECORD_HAS_CHILD_ALIAS);
        ps.appendQuery(" FROM (");

        ps.appendQuery("SELECT ");
        ps.appendQuery("record_id");
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

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }
        if (version == null) {
            ps.appendQuery(", \"record_version_from\", (SELECT MIN(VERSION) FROM " + tableName + " rbo1 where rbo.record_id=rbo1.record_id and rbo1.VERSION>\"record_version_from\") \"record_version_to\" ");
        }

        ps.appendQuery(" FROM ct rbo \n");
        ps.appendQuery("WHERE ");
        ps.appendQuery(uniqueRecordId == null ? "PARENT_ID is null" : "PARENT_ID = " + uniqueRecordId);

        if (pagingParams != null) {
            ps.appendQuery(" and row_number_over BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
        }
        ps.appendQuery(") res");
        return ps;
    }

    @Override
    public int getRecordVersionsCount(String tableName, Long uniqueRecordId) {
        String sql = "select count(*) as cnt from %s where STATUS=" + VersionedObjectStatus.NORMAL.getId() + " and RECORD_ID=(select RECORD_ID from %s where ID=?)";
        return getJdbcTemplate().queryForInt(String.format(sql, tableName, tableName),
                uniqueRecordId);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(String tableName, Long refBookId, Long recordId) {
        return refBookDao.getRecordData(refBookId, tableName, recordId);
    }

    @Override
    public List<Date> getVersions(String tableName, Date startDate, Date endDate) {
        String sql = String.format("SELECT version FROM %s " +
                "where version >= ? and version <= ? GROUP BY version", tableName);
        return getJdbcTemplate().queryForList(sql, new Object[]{startDate, endDate}, new int[]{Types.DATE, Types.DATE}, Date.class);
    }

    @Override
    public Long getRecordId(String tableName, Long uniqueRecordId) {
        try {
            return getJdbcTemplate().queryForLong(String.format("select record_id from %s where id=?",tableName), uniqueRecordId);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найдена запись справочника с id = %d", uniqueRecordId));
        }
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(String tableName, Long refBookId, Date version, Long parentRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        if (filter == null) {
            String fullFilter = RefBook.RECORD_PARENT_ID_ALIAS + (parentRecordId == null ? " is null" : " = " + parentRecordId.toString());
            return getRecords(tableName, refBookId, version, pagingParams, fullFilter, sortAttribute, true, true);
        }
        else {
            return getChildrenRecords(tableName, refBookId, parentRecordId, version, pagingParams, filter, sortAttribute, true);
        }
    }

    private static final String GET_RECORD_VERSION = "with currentVersion as (select id, version, record_id from %s where id = ?),\n" +
            "minNextVersion as (select r.record_id, min(r.version) version from %s r, currentVersion cv where r.version > cv.version and r.record_id= cv.record_id and r.status != -1 group by r.record_id),\n" +
            "nextVersionEnd as (select mnv.record_id, mnv.version, r.status from minNextVersion mnv, %s r where mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
            "select cv.id as %s, \n" +
            "cv.version as versionStart, \n" +
            "nve.version - interval '1' day as versionEnd, \n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from currentVersion cv \n" +
            "left join nextVersionEnd nve on nve.record_id= cv.record_id";

    @Override
    public RefBookRecordVersion getRecordVersionInfo(String tableName, Long uniqueRecordId) {
        try {
            String sql = String.format(GET_RECORD_VERSION, tableName, tableName, tableName, RefBook.RECORD_ID_ALIAS);
            return getJdbcTemplate().queryForObject(sql,
                    new Object[] {
                            uniqueRecordId
                    },
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new DaoException("Не найдены версии для указанного элемента справочника", ex);
        }
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(String tableName, List<Long> uniqueRecordIds) {
        final Map<Long, Date> result = new HashMap<Long, Date>();
        getJdbcTemplate().query(String.format("select id, version from %s where %s", tableName,
                SqlUtils.transformToSqlInStatement("id", uniqueRecordIds)), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                result.put(SqlUtils.getLong(rs,"id"), rs.getDate("version"));
            }
        });
        return result;
    }

    private final static String CHECK_UNIQUE_MATCHES = "select ID from %s where STATUS=0";

    @Override
    public List<Pair<Long,String>> getMatchedRecordsByUniqueAttributes(String tableName, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        String paramRestriction = null;
        List<Pair<Long,String>> matches = new ArrayList<Pair<Long, String>>();
        for (final RefBookAttribute attribute : attributes) {
            if (attribute.getUnique() != 0) {
                StringBuilder query = new StringBuilder(CHECK_UNIQUE_MATCHES).append(" and ");
                for (int i=0; i < records.size(); i++) {
                    Map<String, RefBookValue> values = records.get(i).getValues();
                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        paramRestriction = "%s = '%s'";
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                        paramRestriction = "%s = %s";
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                        paramRestriction = "%s = to_date('%s', 'DD.MM.YYYY')";
                    }
                    query.append(String.format(paramRestriction, attribute.getAlias(), values.get(attribute.getAlias())));

                    if (i < records.size() - 1) {
                        query.append(" or ");
                    }
                }

                String sql = String.format(query.toString(), tableName);

                matches.addAll(getJdbcTemplate().query(sql, new RowMapper<Pair<Long, String>>() {
                    @Override
                    public Pair<Long, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<Long, String>(SqlUtils.getLong(rs,"ID"), attribute.getAlias());
                    }
                }));
            }
        }
        return matches;
    }

    private final static String CHECK_CONFLICT_VALUES_VERSIONS = "with conflictRecord as (select * from %s where %s),\n" +
            "allRecordsInConflictGroup as (select r.* from %s r where exists (select 1 from conflictRecord cr where r.RECORD_ID=cr.RECORD_ID and r.status != -1)),\n" +
            "recordsByVersion as (select ar.*, row_number() over(partition by ar.RECORD_ID order by ar.version) rn from allRecordsInConflictGroup ar),\n" +
            "versionInfo as (select rv.ID, rv.VERSION versionFrom, rv2.version - interval '1' day versionTo from conflictRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.ID=cr.ID)" +
            "select ID from versionInfo where (\n" +
            "\tversionTo IS NOT NULL and (versionFrom <= ? and versionTo >= ?)\n" +
            ") or (\n" +
            "\t\t(versionFrom >= ? and (? IS NULL or versionFrom <= ?))\n" +
            ")";

    @Override
    public void checkConflictValuesVersions(String tableName, List<Pair<Long,String>> recordPairs, Date versionFrom, Date versionTo) {
        List<Long> recordIds = new ArrayList<Long>();
        for (Pair<Long,String> pair : recordPairs) {
            recordIds.add(pair.getFirst());
        }

        String sql = String.format(CHECK_CONFLICT_VALUES_VERSIONS,
                tableName, SqlUtils.transformToSqlInStatement("ID", recordIds), tableName);
        List<Long> conflictedIds = getJdbcTemplate().queryForList(sql, Long.class, versionFrom, versionFrom, versionFrom, versionTo, versionTo);
        if (!conflictedIds.isEmpty()) {
            StringBuilder attrNames = new StringBuilder();
            for (Long id : conflictedIds) {
                for (Pair<Long,String> pair : recordPairs) {
                    if (pair.getFirst().equals(id)) {
                        attrNames.append("'").append(pair.getSecond()).append("',");
                    }
                }
            }
            attrNames.deleteCharAt(attrNames.length()-1);
            throw new DaoException("Нарушено требование к уникальности, уже существует элемент с такими значениями атрибута "+attrNames+" в указанном периоде!");
        }
    }

    @Override
    public boolean isVersionsExist(String tableName, List<Long> recordIds, Date version) {
        String sql = String.format("select count(*) from %s where %s and version = trunc(?, 'DD') and status != -1", tableName, SqlUtils.transformToSqlInStatement("record_id", recordIds));
        return getJdbcTemplate().queryForInt(sql, version) != 0;
    }

    private static final String CHECK_CROSS_VERSIONS = "with allVersions as (select r.* from %s r where record_id=? and (? is null or id=?)),\n" +
            "recordsByVersion as (select r.*, row_number() over(partition by r.record_id order by r.version) rn from %s r, allVersions av where r.id=av.id and r.status != -1),\n" +
            "versionInfo as (select rv.rn NUM, rv.ID, rv.VERSION, rv.status, rv2.version - interval '1' day nextVersion,rv2.status nextStatus from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn)\n" +
            "select num, id, version, status, nextversion, nextstatus, \n" +
            "case\n" +
            "  when (status=0 and (\n" +
            "  \t(? is null and (\n" +
            "  \t\t(nextversion is not null and nextversion > ?) or \n" +
            "\t\t(nextversion is null and version > ?)\n" +
            "  \t)) or (? is not null and (\n" +
            "  \t\t(version < ? and nextversion > ?) or \n" +
            "  \t\t(version > ? and version < ?)\n" +
            "  \t))\n" +
            "  )) then 1\n" +
            "  when (status=0 and nextversion is null and version < ?) then 2\n" +
            "  when (status=2 and (? is not null and version > ? and version < ? and nextversion > ?)) then 3\n" +
            "  when (status=2 and (\n" +
            "  \t(nextversion is not null and ? is null and version >= ?) or \n" +
            "  \t(nextversion is null and version >= ?)\n" +
            "  )) then 4\n" +
            "  else 0\n" +
            "end as result\n" +
            "from versionInfo";

    @Override
    public List<CheckCrossVersionsResult> checkCrossVersions(String tableName, Long recordId, Date versionFrom, Date versionTo, Long excludedRecordId) {
        return getJdbcTemplate().query(String.format(CHECK_CROSS_VERSIONS, tableName, tableName),
                new RowMapper<CheckCrossVersionsResult>() {
            @Override
            public CheckCrossVersionsResult mapRow(ResultSet rs, int rowNum) throws SQLException {
                CheckCrossVersionsResult result = new CheckCrossVersionsResult();
                result.setNum(SqlUtils.getInteger(rs,"NUM"));
                result.setRecordId(SqlUtils.getLong(rs,"ID"));
                result.setVersion(rs.getDate("VERSION"));
                result.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs,"STATUS")));
                result.setNextVersion(rs.getDate("NEXTVERSION"));
                result.setNextStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs,"NEXTSTATUS")));
                result.setResult(CrossResult.getResultById(SqlUtils.getInteger(rs,"RESULT")));
                return result;
            }
        }, recordId, excludedRecordId, excludedRecordId,
                versionTo, versionFrom, versionFrom, versionTo, versionFrom, versionFrom, versionFrom, versionTo,
                versionFrom, versionTo, versionFrom, versionTo, versionTo, versionTo, versionFrom, versionFrom);
    }

    @Override
    public boolean isVersionUsed(String tableName, Long refBookId, Long uniqueRecordId, Date versionFrom) {
        //TODO добавить проверки по другим точкам запросов
        //Проверка использования в справочниках и настройках подразделений
        String sql = String.format("select count(r.id) from %s r, ref_book_record rr, ref_book_value v where v.attribute_id in (select id from ref_book_attribute where ref_book_id=?) and rr.version >= ? and v.REFERENCE_VALUE=r.id and v.RECORD_ID=rr.id and r.id=?", tableName);
        return getJdbcTemplate().queryForInt(sql, refBookId, versionFrom, uniqueRecordId) != 0;
    }

    private static final String CHECK_USAGES_IN_REFBOOK = "with checkRecords as (select * from %s where %s)\n" +
            "select count(r.id) from ref_book_value v, checkRecords cr, ref_book_record r where v.attribute_id in (select id from ref_book_attribute where ref_book_id=?) and r.version >= cr.version and cr.id=v.reference_value and v.record_id=r.id";

    private static final String CHECK_USAGES_IN_FORMS = "select count(*) from form_data_ref_book where ref_book_id=? and %s";

    @Override
    public boolean isVersionUsed(String tableName, Long refBookId, List<Long> uniqueRecordIds) {
        //Проверка использования в справочниках и настройках подразделений
        String idIn = SqlUtils.transformToSqlInStatement("id", uniqueRecordIds);
        String sql = String.format(CHECK_USAGES_IN_REFBOOK, tableName, idIn);
        boolean hasReferences = getJdbcTemplate().queryForInt(sql, refBookId) != 0;
        if (!hasReferences) {
            String recordIdIn = SqlUtils.transformToSqlInStatement("record_id", uniqueRecordIds);
            sql = String.format(CHECK_USAGES_IN_FORMS, recordIdIn);
            return getJdbcTemplate().queryForInt(sql, refBookId) != 0;
        } else return true;
    }

    private static final String GET_NEXT_RECORD_VERSION = "with nextVersion as (select r.* from %s r where r.record_id = ? and r.status != -1 and r.version  = \n" +
            "\t(select min(version) from %s where record_id=r.record_id and status=0 and version > ?)),\n" +
            "minNextVersion as (select r.record_id, min(r.version) version from %s r, nextVersion nv where r.version > nv.version and r.record_id= nv.record_id and r.status != -1 group by r.record_id),\n" +
            "nextVersionEnd as (select mnv.record_id, mnv.version, r.status from minNextVersion mnv, %s r where mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
            "select nv.id as %s, nv.version as versionStart, nve.version - interval '1' day as versionEnd,\n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from nextVersion nv \n" +
            "left join nextVersionEnd nve on (nve.record_id= nv.record_id)";

    @Override
    public RefBookRecordVersion getNextVersion(String tableName, Long recordId, Date versionFrom) {
        String sql = String.format(GET_NEXT_RECORD_VERSION, tableName, tableName, tableName, tableName, RefBook.RECORD_ID_ALIAS);
        try {
            return getJdbcTemplate().queryForObject(sql,
                    new Object[] {
                            recordId, versionFrom
                    },
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String INSERT_FAKE_REF_BOOK_RECORD_SQL = "insert into %s (id, record_id, version," +
            "status) values (seq_ref_book_oktmo.nextval, ?, ?, 2)";

    @Override
    public void createFakeRecordVersion(String tableName, Long recordId, Date version) {
        getJdbcTemplate().update(String.format(INSERT_FAKE_REF_BOOK_RECORD_SQL, tableName),
                recordId, version);
    }

    private static final String INSERT_REF_BOOK_OKTMO_SQL = "insert into %s (id, record_id, version, status, %s) " +
            "values (?, ?, ?, ?, %s)";

    @Override
    public List<Long> createRecordVersion(String tableName, Long refBookId, final Date version, final VersionedObjectStatus status, final List<RefBookRecord> records) {
        if (records == null || records.isEmpty()) {
            return null;
        }

        RefBook refBook = refBookDao.get(refBookId);
        final List<RefBookAttribute> attributes = refBook.getAttributes();
        StringBuilder selectPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();
        for (int i=0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            selectPart.append(attribute.getAlias());
            valuesPart.append("?");
            if (i < attributes.size() - 1) {
                selectPart.append(", ");
                valuesPart.append(", ");
            }
        }

        final List<Long> refBookRecordIds  = dbUtils.getNextIds(BDUtils.Sequence.REF_BOOK_OKTMO, (long) records.size());
        BatchPreparedStatementSetter batchRefBookRecordsPS = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RefBookRecord record = records.get(i);
                ps.setLong(1, refBookRecordIds.get(i));
                ps.setLong(2, record.getRecordId());
                ps.setDate(3, new java.sql.Date(version.getTime()));
                ps.setLong(4, status.getId());
                for (int j = 0; j < attributes.size(); j++) {
                    RefBookAttribute attribute = attributes.get(j);
                    RefBookValue value = record.getValues().get(attribute.getAlias());
                    switch (attribute.getAttributeType()) {
                        case STRING: {
                            ps.setString(5 + j, value.getStringValue());
                        }
                        break;
                        case NUMBER: {
                            if (value.getNumberValue() != null) {
                                ps.setDouble(5 + j, BigDecimal.valueOf(value.getNumberValue().doubleValue())
                                        .setScale(attribute.getPrecision(), RoundingMode.HALF_UP).doubleValue());
                            }
                        }
                        break;
                        case DATE: {
                            ps.setDate(5 + j, new java.sql.Date(value.getDateValue().getTime()));
                        }
                        break;
                        case REFERENCE: {
                            ps.setLong(5 + j, value.getReferenceValue());
                        }
                        break;
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return refBookRecordIds.size();
            }
        };

        String sql = String.format(INSERT_REF_BOOK_OKTMO_SQL,
                tableName,
                selectPart,
                valuesPart
        );

        getJdbcTemplate().batchUpdate(sql, batchRefBookRecordsPS);
        return refBookRecordIds;
    }

    private static final String UPDATE_REF_BOOK_OKTMO_SQL = "update %s set %s where id = ?";

    @Override
    public void updateRecordVersion(String tableName, Long refBookId, final Long uniqueRecordId, final Map<String, RefBookValue> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        RefBook refBook = refBookDao.get(refBookId);
        final List<RefBookAttribute> attributes = refBook.getAttributes();
        StringBuilder params = new StringBuilder();
        for (int i=0; i < attributes.size(); i++) {
            RefBookAttribute attribute = attributes.get(i);
            params.append(attribute.getAlias()).append(" = ").append("?");
            if (i < attributes.size() - 1) {
                params.append(", ");
            }
        }

        String sql = String.format(UPDATE_REF_BOOK_OKTMO_SQL,
                tableName,
                params
        );

        getJdbcTemplate().update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                int i = 0;
                for (; i < attributes.size(); i++) {
                    RefBookAttribute attribute = attributes.get(i);
                    RefBookValue value = values.get(attribute.getAlias());
                    switch (attribute.getAttributeType()) {
                        case STRING: {
                            ps.setString(i + 1, value.getStringValue());
                        }
                        break;
                        case NUMBER: {
                            if (value.getNumberValue() != null) {
                                ps.setDouble(i + 1, BigDecimal.valueOf(value.getNumberValue().doubleValue())
                                        .setScale(attribute.getPrecision(), RoundingMode.HALF_UP).doubleValue());
                            }
                        }
                        break;
                        case DATE: {
                            ps.setDate(i + 1, new java.sql.Date(value.getDateValue().getTime()));
                        }
                        break;
                        case REFERENCE: {
                            ps.setLong(i + 1, value.getReferenceValue());
                        }
                        break;
                    }
                }
                ps.setLong(i + 1, uniqueRecordId);
            }
        });
    }

    private static final String GET_RELATED_VERSIONS = "with currentRecord as (select id, record_id from %s where %s),\n" +
            "recordsByVersion as (select r.ID, r.RECORD_ID, STATUS, VERSION, row_number() over(partition by r.RECORD_ID order by r.version) rn from %s r, currentRecord cr where r.record_id=cr.record_id and r.status != -1) \n" +
            "select rv2.ID from currentRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where cr.id=rv.id and rv2.status=?";

    @Override
    public List<Long> getRelatedVersions(String tableName, List<Long> uniqueRecordIds) {
        try {
            String sql = String.format(GET_RELATED_VERSIONS,
                    tableName, SqlUtils.transformToSqlInStatement("id", uniqueRecordIds), tableName);
            return getJdbcTemplate().queryForList(sql, Long.class, VersionedObjectStatus.FAKE.getId());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
    }

    private final static String GET_FIRST_RECORD_ID = "with allRecords as (select id, version from %s where record_id = (select record_id from %s where id = ?) and id != ?)\n" +
            "select id from allRecords where version = (select min(version) from allRecords)";

    @Override
    public Long getFirstRecordId(String tableName, Long refBookId, Long uniqueRecordId) {
        try {
            return getJdbcTemplate().queryForLong(String.format(GET_FIRST_RECORD_ID, tableName, tableName), uniqueRecordId, uniqueRecordId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Date> hasChildren(String tableName, List<Long> uniqueRecordIds) {
        String sql = String.format("select distinct version from %s where %s",
                tableName, SqlUtils.transformToSqlInStatement("parent_id", uniqueRecordIds));
        try {
            return getJdbcTemplate().query(sql, new RowMapper<Date>() {
                @Override
                public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getDate(1);
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private final static String GET_ATTRIBUTES_VALUES = "select \n" +
            "  attribute_id,\n" +
            "  record_id, \n" +
            "  value,\n" +
            "  data_type\n" +
            "from (\n" +
            "  with t as (\n" +
            "  select id as record_id, to_char(id) as id, to_char(code) as code, to_char(parent_id) as parent_id, name from ref_book_oktmo \n" +
            "  )\n" +
            "  select a.id as attribute_id, a.type as data_type, record_id, value from t\n" +
            "  unpivot \n" +
            "  (value for attribute_alias in (NAME, CODE, ID, PARENT_ID)) \n" +
            "  join ref_book_attribute a on attribute_alias = a.alias\n" +
            "  where ref_book_id = 96\n" +
            ") where (record_id, attribute_id) in ";

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        final Map<RefBookAttributePair, String> result = new HashMap<RefBookAttributePair, String>();
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(GET_ATTRIBUTES_VALUES);
        ps.appendQuery("(");
        for (Iterator<RefBookAttributePair> it = attributePairs.iterator(); it.hasNext();) {
            RefBookAttributePair pair = it.next();
            ps.appendQuery("(?,?)");
            ps.addParam(pair.getUniqueRecordId());
            ps.addParam(pair.getAttributeId());
            if (it.hasNext()) {
                ps.appendQuery(",");
            }
        }
        ps.appendQuery(")");
        getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(),
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        result.put(new RefBookAttributePair(rs.getLong("attribute_id"), rs.getLong("record_id")), rs.getString("value"));
                    }
                });
        return result;
    }

    @Override
    public List<Long> getUniqueRecordIds(Long refBookId, String tableName, Date version, String filter) {
        RefBook refBook = refBookDao.get(refBookId);
        PreparedStatementData ps = getSimpleQuery(tableName, refBook, null, null, version, null, filter, null, false, null, true, false);
        return getJdbcTemplate().queryForList(ps.getQuery().toString(), ps.getParams().toArray(), Long.class);
    }

    @Override
    public int getRecordsCount(Long refBookId, String tableName, Date version, String filter) {
        RefBook refBook = refBookDao.get(refBookId);
        PreparedStatementData ps = getSimpleQuery(tableName, refBook, null, null, version, null, filter, null, false, null, true, false);
        return refBookDao.getRecordsCount(ps);
    }

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(String tableName, @NotNull Long refBookId, Date version, Boolean needAccurateVersion, String filter) {
        // TODO сейчас параметры version и needAccurateVersion игнорируются
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("SELECT ");
        ps.appendQuery("id, RECORD_ID ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        ps.appendQuery(" FROM ");
        ps.appendQuery(tableName);
        ps.appendQuery(" frb");

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener = applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE ");
            ps.appendQuery(filterPS.getQuery().toString());
            if (!filterPS.getParams().isEmpty()) {
                ps.addParam(filterPS.getParams());
            }
        }

        try {
            return getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(),
                    new RowMapper<Pair<Long, Long>>() {
                        @Override
                        public Pair<Long, Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new Pair<Long, Long>(SqlUtils.getLong(rs, "ID"), SqlUtils.getLong(rs, "RECORD_ID"));
                        }
                    });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(String tableName, Long refBookId, Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        RefBook refBook = refBookDao.get(refBookId);
        RefBook refBookClone = SerializationUtils.clone(refBook);
        refBookClone.setAttributes(new ArrayList<RefBookAttribute>());
        refBookClone.getAttributes().addAll(refBook.getAttributes());

        // Получение количества данных в справочнике
        PreparedStatementData ps = getSimpleQuery(tableName, refBookClone, recordId, null, null, sortAttribute, filter, pagingParams, false, null, false, false);
        Integer recordsCount = refBookDao.getRecordsCount(ps);

        ps = getSimpleQuery(tableName, refBookClone, recordId, null, null, sortAttribute, filter, pagingParams, false, null, false, false);
        refBookClone.addAttribute(RefBook.getVersionFromAttribute());
        refBookClone.addAttribute(RefBook.getVersionToAttribute());

        List<Map<String, RefBookValue>> records = refBookDao.getRecordsData(ps, refBook);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(recordsCount);
        return result;
    }
}
