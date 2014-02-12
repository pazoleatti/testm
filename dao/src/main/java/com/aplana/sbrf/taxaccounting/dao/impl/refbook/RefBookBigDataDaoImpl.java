package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.SimpleFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookBigDataDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author dloshkarev
 */
@Repository
public class RefBookBigDataDaoImpl extends AbstractDao implements RefBookBigDataDao {

    @Autowired
    private RefBookUtils refBookUtils;

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    private BDUtils dbUtils;

    @Autowired
    private ApplicationContext applicationContext;

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(String tableName, Long refBookId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);
        // получаем страницу с данными
        PreparedStatementData ps = getSimpleQuery(tableName, refBook, null, version, sortAttribute, filter, pagingParams, isSortAscending, null);
        System.out.println("ps: "+ps);
        List<Map<String, RefBookValue>> records = refBookUtils.getRecordsData(ps, refBook);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        // получаем информацию о количестве всех записей с текущим фильтром
        ps = getSimpleQuery(tableName, refBook, null, version, sortAttribute, filter, null, isSortAscending, null);
        result.setTotalCount(refBookUtils.getRecordsCount(ps));
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
        PreparedStatementData ps = getSimpleQuery(tableName, refBook, uniqueRecordId, null, sortAttribute, filter, pagingParams, isSortAscending, null);

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
            "with t as (select\n" +
                    "  max(version) version, record_id\n" +
                    "from\n" +
                    "  %s\n" +
                    "where\n" +
                    "  status = 0 and version <= ?\n" +
                    "group by\n" +
                    "  record_id)\n";

    private static final String RECORD_VERSIONS_STATEMENT =
            "with currentRecord as (select id, record_id, version from %s where id=?),\n" +
                    "recordsByVersion as (select r.ID, r.RECORD_ID, r.VERSION, r.STATUS, row_number() over(partition by r.RECORD_ID order by r.version) rn from %s r, currentRecord cr where r.RECORD_ID=cr.RECORD_ID), \n" +
                    "t as (select rv.rn as row_number_over, rv.ID, rv.RECORD_ID RECORD_ID, rv.VERSION version, rv2.version versionEnd from recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.status=?)\n";


    /**
     * Формирует простой sql-запрос по принципу: один справочник - одна таблица
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
    private PreparedStatementData getSimpleQuery(String tableName, RefBook refBook, Long uniqueRecordId, Date version, RefBookAttribute sortAttribute,
                                                String filter, PagingParams pagingParams, boolean isSortAscending, String whereClause) {
        PreparedStatementData ps = new PreparedStatementData();

        if (version != null) {
            ps.appendQuery(String.format(WITH_STATEMENT, tableName));
            ps.addParam(version);
        } else {
            ps.appendQuery(String.format(RECORD_VERSIONS_STATEMENT, tableName, tableName));
            ps.addParam(uniqueRecordId);
            ps.addParam(VersionedObjectStatus.NORMAL.getId());
        }

        ps.appendQuery("SELECT ");
        ps.appendQuery("r.id ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);

        if (version == null) {
            ps.appendQuery(",\n");
            ps.appendQuery("  t.version as ");
            ps.appendQuery(RefBook.RECORD_VERSION_FROM_ALIAS);
            ps.appendQuery(",\n");

            ps.appendQuery("  t.versionEnd as ");
            ps.appendQuery(RefBook.RECORD_VERSION_TO_ALIAS);
        }

        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }

        if (version != null) {
            ps.appendQuery(" FROM t, (SELECT ");
            if (isSupportOver()) {
                RefBookAttribute defaultSort = refBook.getSortAttribute();
                String sortColumn = sortAttribute == null ? (defaultSort == null ? "id" : defaultSort.getAlias()) : sortAttribute.getAlias();
                String sortDirection = isSortAscending ? "ASC" : "DESC";
                ps.appendQuery("row_number() over (order by '" + sortColumn + "' "+sortDirection+") as row_number_over");
            } else {
                ps.appendQuery("rownum row_number_over");
            }
            ps.appendQuery(", t.* FROM ");
            ps.appendQuery(tableName);
            ps.appendQuery(" t ) r");
        } else {
            ps.appendQuery(" FROM t, ");
            ps.appendQuery(tableName);
            ps.appendQuery(" r");
        }

        PreparedStatementData filterPS = new PreparedStatementData();
        SimpleFilterTreeListener simpleFilterTreeListener =  applicationContext.getBean("simpleFilterTreeListener", SimpleFilterTreeListener.class);
        simpleFilterTreeListener.setRefBook(refBook);
        simpleFilterTreeListener.setPs(filterPS);

        Filter.getFilterQuery(filter, simpleFilterTreeListener);
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE (");
            ps.appendQuery(filterPS.getQuery().toString());
            if (filterPS.getParams().size() > 0) {
                ps.addParam(filterPS.getParams());
            }
            ps.appendQuery(") ");
        }
        if (whereClause != null && whereClause.trim().length() > 0) {
            if (filterPS.getQuery().length() > 0) {
                ps.appendQuery(" AND ");
            } else {
                ps.appendQuery(" WHERE ");
            }
            ps.appendQuery(whereClause);
        }

        if (filterPS.getQuery().length() > 0 ||
                (whereClause != null && whereClause.trim().length() > 0 && filterPS.getQuery().length() == 0)) {
            ps.appendQuery(" and ");
        } else {
            ps.appendQuery(" where ");
        }
        ps.appendQuery("(r.version = t.version and r.record_id = t.record_id)");

        if (pagingParams != null) {
            ps.appendQuery(" and row_number_over BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
        }

        if (version == null) {
            ps.appendQuery(" order by t.version\n");
        }
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
        return refBookUtils.getRecordData(refBookId, tableName, recordId);
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
        if (filter == null || filter.isEmpty()) {
            filter = " parent_id = " + parentRecordId;
        } else {
            filter += " AND parent_id = " + parentRecordId;
        }
        return getRecords(tableName, refBookId, version, pagingParams, filter, sortAttribute, true);
    }

    private static final String GET_RECORD_VERSION = "with currentRecord as (select r.id, r.record_id, r.version from %s r where r.id=?),\n" +
            "nextVersion as (select min(r.version) as version from %s r, currentRecord cr where r.version > cr.version and r.record_id=cr.record_id)\n" +
            "select cr.id as %s, cr.version as versionStart, nv.version as versionEnd from currentRecord cr, nextVersion nv";

    @Override
    public RefBookRecordVersion getRecordVersionInfo(String tableName, Long uniqueRecordId) {
        try {
            String sql = String.format(GET_RECORD_VERSION, tableName, tableName, RefBook.RECORD_ID_ALIAS);
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
        getJdbcTemplate().query(String.format("select id, version from %s where id in %s", tableName,
                SqlUtils.transformToSqlInStatement(uniqueRecordIds)), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                result.put(rs.getLong("id"), rs.getDate("version"));
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
            if (attribute.isUnique()) {
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
                        return new Pair<Long, String>(rs.getLong("ID"), attribute.getAlias());
                    }
                }));
            }
        }
        return matches;
    }

    private final static String CHECK_CONFLICT_VALUES_VERSIONS = "with conflictRecord as (select * from %s where ID in %s),\n" +
            "allRecordsInConflictGroup as (select r.* from %s r where exists (select 1 from conflictRecord cr where r.RECORD_ID=cr.RECORD_ID)),\n" +
            "recordsByVersion as (select ar.*, row_number() over(partition by ar.RECORD_ID order by ar.version) rn from allRecordsInConflictGroup ar),\n" +
            "versionInfo as (select rv.ID, rv.VERSION versionFrom, rv2.version versionTo from conflictRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where rv.ID=cr.ID)" +
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
                tableName, SqlUtils.transformToSqlInStatement(recordIds), tableName);
        List<Long> conflictedIds = getJdbcTemplate().queryForList(sql, Long.class, versionFrom, versionFrom, versionFrom, versionTo, versionTo);
        if (conflictedIds.size() > 0) {
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
        String sql = String.format("select count(*) from %s where record_id in %s and version = trunc(?, 'DD')", tableName, SqlUtils.transformToSqlInStatement(recordIds));
        return getJdbcTemplate().queryForInt(sql, version) != 0;
    }

    private static final String CHECK_CROSS_VERSIONS = "with allVersions as (select r.* from %s r where record_id=? and (? is null or id=?)),\n" +
            "recordsByVersion as (select r.*, row_number() over(partition by r.record_id order by r.version) rn from %s r, allVersions av where r.id=av.id),\n" +
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
            "  \t(nextversion is not null and ? is null and version > ?) or \n" +
            "  \t(nextversion is null and version > ?)\n" +
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
                result.setNum(rs.getInt("NUM"));
                result.setRecordId(rs.getLong("ID"));
                result.setVersion(rs.getDate("VERSION"));
                result.setStatus(VersionedObjectStatus.getStatusById(rs.getInt("STATUS")));
                result.setNextVersion(rs.getDate("NEXTVERSION"));
                result.setNextStatus(VersionedObjectStatus.getStatusById(rs.getInt("NEXTSTATUS")));
                result.setResult(CrossResult.getResultById(rs.getInt("RESULT")));
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

    private static final String CHECK_USAGES_IN_REFBOOK = "with checkRecords as (select * from %s where id in %s)\n" +
            "select count(r.id) from ref_book_value v, checkRecords cr, ref_book_record r where v.attribute_id in (select id from ref_book_attribute where ref_book_id=?) and r.version >= cr.version and cr.id=v.reference_value and v.record_id=r.id";

    private static final String CHECK_USAGES_IN_FORMS = "select count(*) from numeric_value where column_id in (select id from form_column " +
            "where attribute_id in (select attribute_id from ref_book_value where attribute_id in (select id from ref_book_attribute where ref_book_id=?) and record_id in %s)) and value in %s";

    @Override
    public boolean isVersionUsed(String tableName, Long refBookId, List<Long> uniqueRecordIds) {
        //Проверка использования в справочниках и настройках подразделений
        String in = SqlUtils.transformToSqlInStatement(uniqueRecordIds);
        String sql = String.format(CHECK_USAGES_IN_REFBOOK, tableName, in);
        boolean hasReferences = getJdbcTemplate().queryForInt(sql, refBookId) != 0;
        if (!hasReferences) {
            sql = String.format(CHECK_USAGES_IN_FORMS, in, in);
            return getJdbcTemplate().queryForInt(sql, refBookId) != 0;
        } else return true;
    }

    private static final String GET_NEXT_RECORD_VERSION = "with nextVersion as (select r.* from %s r where r.record_id=? and r.status=0 and r.version > ?),\n" +
            "nextVersionEnd as (select min(r.version) as versionEnd from %s r, nextVersion nv where r.version > nv.version and r.record_id=nv.record_id)\n" +
            "select nv.id as %s, nv.version as versionStart, nve.versionEnd from nextVersion nv, nextVersionEnd nve";

    @Override
    public RefBookRecordVersion getNextVersion(String tableName, Long recordId, Date versionFrom) {
        String sql = String.format(GET_NEXT_RECORD_VERSION, tableName, tableName, RefBook.RECORD_ID_ALIAS);
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
    public void createRecordVersion(String tableName, Long refBookId, final Date version, final VersionedObjectStatus status, final List<RefBookRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
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

    private static final String GET_RELATED_VERSIONS = "with currentRecord as (select id, record_id from %s where id in %s),\n" +
            "recordsByVersion as (select r.ID, r.RECORD_ID, STATUS, VERSION, row_number() over(partition by r.RECORD_ID order by r.version) rn from %s r, currentRecord cr where r.record_id=cr.record_id) \n" +
            "select rv2.ID from currentRecord cr, recordsByVersion rv left outer join recordsByVersion rv2 on rv.RECORD_ID = rv2.RECORD_ID and rv.rn+1 = rv2.rn where cr.id=rv.id and rv2.status=?";

    @Override
    public List<Long> getRelatedVersions(String tableName, List<Long> uniqueRecordIds) {
        try {
            String sql = String.format(GET_RELATED_VERSIONS,
                    tableName, SqlUtils.transformToSqlInStatement(uniqueRecordIds), tableName);
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
        String sql = String.format("select distinct version from %s where parent_id in %s",
                tableName, SqlUtils.transformToSqlInStatement(uniqueRecordIds));
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
}
