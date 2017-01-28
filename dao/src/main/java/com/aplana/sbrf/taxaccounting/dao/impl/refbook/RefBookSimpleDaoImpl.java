package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.RefBookSimpleQueryBuilderComponent;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * Имплементация DAO для RefBookSimpleDataProvider, поддерживающая редактируемые версионируемые справочники.
 * Такие справочники должны иметь поля STATUS, VERSION, RECORD_ID
 */
@Repository
public class RefBookSimpleDaoImpl extends AbstractDao implements RefBookSimpleDao {

    private static final Log LOG = LogFactory.getLog(RefBookSimpleDaoImpl.class);

    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookSimpleQueryBuilderComponent queryBuilder;
    @Autowired
    private BDUtils dbUtils;

    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBook, null, null, version, sortAttribute, filter, pagingParams, isSortAscending, false);

        List<Map<String, RefBookValue>> records = refBookDao.getRecordsData(ps, refBook);

        ps = queryBuilder.psGetRecordsQuery(refBook, null, null, version, sortAttribute, filter, null, isSortAscending, false);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(String tableName, Long refBookId, Date version,
                                                                      Long parentRecordId, PagingParams pagingParams,
                                                                      String filter, RefBookAttribute sortAttribute) {
        return getChildrenRecords(refBookId, version, parentRecordId, pagingParams, filter, sortAttribute, true);
    }

    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long refBookId, Date version,
                                                                      Long parentRecordId, PagingParams pagingParams,
                                                                      String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);

        if (refBook.isHierarchic() && parentRecordId == null && filter == null) {
            String fullFilter = RefBook.RECORD_PARENT_ID_ALIAS + " is null";
            return getRecords(refBookId, version, pagingParams, fullFilter, sortAttribute, true);
        } else if (!refBook.isHierarchic()){
            throw new IllegalArgumentException(String.format(RefBookDaoImpl.NOT_HIERARCHICAL_REF_BOOK_ERROR, refBook.getName(), refBook.getId()));
        }

        PreparedStatementData ps = queryBuilder.psGetChildrenRecordsQuery(refBook, parentRecordId, version, sortAttribute, filter, pagingParams, isSortAscending);
        List<Map<String, RefBookValue>> records = refBookDao.getRecordsWithHasChild(ps, refBook);

        ps = queryBuilder.psGetChildrenRecordsQuery(refBook, parentRecordId, version, sortAttribute, filter, null, isSortAscending);

        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    public Long getRowNum(@NotNull Long refBookId, Date version, Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBook, recordId, null, version, sortAttribute, filter, null, isSortAscending, false);

        return refBookDao.getRowNum(ps, recordId);
    }

    public List<Long> getUniqueRecordIds(Long refBookId, String tableName, Date version, String filter) {
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBook, null, null, version, null, filter, null, false, true);

        return getJdbcTemplate().queryForList(ps.getQuery().toString(), ps.getParams().toArray(), Long.class);
    }

    public int getRecordsCount(Long refBookId, String tableName, Date version, String filter) {
        RefBook refBook = refBookDao.get(refBookId);
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBook, null, null, version, null, filter, null, false, false);
        return refBookDao.getRecordsCount(ps);
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

    public RefBookRecordVersion getRecordVersionInfo(String tableName, Long uniqueRecordId) {
        try {
            String sql = String.format(GET_RECORD_VERSION, tableName, tableName, tableName, RefBook.RECORD_ID_ALIAS);
            return getJdbcTemplate().queryForObject(sql,
                    new Object[]{
                            uniqueRecordId
                    },
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException ex) {
            throw new DaoException("Не найдены версии для указанного элемента справочника", ex);
        }
    }

    public List<Date> getVersions(String tableName, Date startDate, Date endDate) {
        String sql = String.format("SELECT version FROM %s " +
                "where version >= ? and version <= ? GROUP BY version", tableName);
        return getJdbcTemplate().queryForList(sql, new Object[]{startDate, endDate}, new int[]{Types.DATE, Types.DATE}, Date.class);
    }

    public int getRecordVersionsCount(String tableName, Long uniqueRecordId) {
        String sql = "select count(*) as cnt from %s where STATUS=" + VersionedObjectStatus.NORMAL.getId() + " and RECORD_ID=(select RECORD_ID from %s where ID=?)";
        return getJdbcTemplate().queryForObject(String.format(sql, tableName, tableName), Integer.class, uniqueRecordId);
    }

    static final String SQL_GET_RECORD_ID = "select record_id from %s where id = %d";
    @Override
    public Long getRecordId(String tableName, Long uniqueRecordId) {
        try {
            return getJdbcTemplate().queryForObject(String.format(SQL_GET_RECORD_ID, tableName, uniqueRecordId), Long.class);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не найдена запись справочника с id = %d", uniqueRecordId));
        }
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long refBookId, Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        RefBook refBook = refBookDao.get(refBookId);
        RefBook refBookClone = SerializationUtils.clone(refBook);
        refBookClone.setAttributes(new ArrayList<RefBookAttribute>());
        refBookClone.getAttributes().addAll(refBook.getAttributes());

        // Получение количества данных в справочнике
        PreparedStatementData ps = queryBuilder.psGetRecordsQuery(refBookClone, recordId, null, null, sortAttribute, filter, pagingParams, false, false);
        Integer recordsCount = refBookDao.getRecordsCount(ps);

        refBookClone.addAttribute(RefBook.getVersionFromAttribute());
        refBookClone.addAttribute(RefBook.getVersionToAttribute());

        List<Map<String, RefBookValue>> records = refBookDao.getRecordsData(ps, refBookClone);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(recordsCount);
        return result;
    }

    @Override
    public List<Pair<Long, String>> getMatchedRecordsByUniqueAttributes(RefBook refBook, Long uniqueRecordId,
                                                                        RefBookRecord record) {

        List<RefBookAttribute> attributes = refBook.getAttributes();
        Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> recordsGroupsUniqueAttributesValues =
                aggregateUniqueAttributesAndValues(attributes, record);

        if (recordsGroupsUniqueAttributesValues.isEmpty()) {
            return new ArrayList<Pair<Long, String>>();
        }

        PreparedStatementData ps = queryBuilder.psGetMatchedRecordsByUniqueAttributes(refBook, uniqueRecordId, record,
                recordsGroupsUniqueAttributesValues);

        List<Pair<Long, String>> result = getNamedParameterJdbcTemplate().query(ps.getQueryString(), ps.getNamedParams(),
                new RowMapper<Pair<Long, String>>() {
                    @Override
                    public Pair<Long, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<Long, String>(SqlUtils.getLong(rs, "id"), rs.getString("name"));
                    }
                });

        return !result.isEmpty() ? aggregateUniqueAttributeNamesByRecords(result) : result;
    }

    /**
     * Агрегировать список пар уникальный атрибут - его значение по группам уникальности и по записям
     *
     * @param attributes атрибуты справочника
     * @param record    запись
     * @return список записей с группами уникальности списков пар уникальных атрибутов и значений
     */
    Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> aggregateUniqueAttributesAndValues(List<RefBookAttribute> attributes, RefBookRecord record) {
        Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> attributeValues = new HashMap<Integer, List<Pair<RefBookAttribute, RefBookValue>>>();
        for (RefBookAttribute attribute : attributes) {
            if (attribute.getUnique() != 0) {
                List<Pair<RefBookAttribute, RefBookValue>> values;
                if (attributeValues.get(attribute.getUnique()) != null) {
                    values = attributeValues.get(attribute.getUnique());
                } else {
                    values = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
                }
                values.add(new Pair<RefBookAttribute, RefBookValue>(attribute, record.getValues().get(attribute.getAlias())));
                attributeValues.put(attribute.getUnique(), values);
            }
        }
        return attributeValues;
    }

    /**
     * Агрегировать названия уникальных аттрибутов по записям
     *
     * @param result список пар идентификатор записи - название уникального атрибута
     * @return список пар идентификатор записи - названия уникальных атрибутов через запятую
     */
    List<Pair<Long, String>> aggregateUniqueAttributeNamesByRecords(List<Pair<Long, String>> result) {
        List<Pair<Long, String>> matchedRecords = new ArrayList<Pair<Long, String>>();
        Long prevRecordId = 0L;
        String prevName = "";
        for (Pair<Long, String> pair : result) {

            if (!prevRecordId.equals(pair.getFirst()) && prevRecordId != 0) {
                Pair<Long, String> newPair = new Pair<Long, String>(prevRecordId, prevName);
                matchedRecords.add(newPair);
            }

            if (prevRecordId.equals(pair.getFirst())) {
                prevName = prevName + ", " + pair.getSecond();
            } else {
                prevName = pair.getSecond();
            }

            prevRecordId = pair.getFirst();
        }

        Pair<Long, String> newPair = new Pair<Long, String>(prevRecordId, prevName);
        matchedRecords.add(newPair);
        return matchedRecords;
    }

    @Override
    public List<Long> checkConflictValuesVersions(RefBook refBook, List<Pair<Long, String>> recordPairs, Date versionFrom, Date versionTo) {
        List<Long> recordIds = new ArrayList<Long>();
        for (Pair<Long, String> pair : recordPairs) {
            recordIds.add(pair.getFirst());
        }
        PreparedStatementData ps = queryBuilder.psCheckConflictValuesVersions(refBook, recordIds, versionFrom, versionTo);
        return getNamedParameterJdbcTemplate().queryForList(ps.getQueryString(), ps.getNamedParams(), Long.class);
    }

    @Override
    public List<Pair<Long, Integer>> checkParentConflict(RefBook refBook, Date versionFrom, List<RefBookRecord> records) {
        final Set<Pair<Long, Integer>> result = new HashSet<Pair<Long, Integer>>();
        for (RefBookRecord record : records) {
            Long parentId = record.getValues().get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue();
            if (parentId != null) {
                PreparedStatementData ps = queryBuilder.psCheckParentConflict(refBook, parentId, versionFrom, record.getVersionTo());
                getNamedParameterJdbcTemplate().query(ps.getQueryString(), ps.getNamedParams(), new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        result.add(new Pair<Long, Integer>(SqlUtils.getLong(rs, "id"), SqlUtils.getInteger(rs, "result")));
                    }
                });
            }
        }
        return new ArrayList<Pair<Long, Integer>>(result);
    }

    @Override
    public List<CheckCrossVersionsResult> checkCrossVersions(RefBook refBook, Long recordId,
                                                             Date versionFrom, Date versionTo, Long excludedRecordId) {


        Map<String, Object> params = new HashMap<String, Object>();
        params.put("refBookId", refBook.getId());
        params.put("recordId", recordId);
        params.put("excludedRecordId", excludedRecordId);
        params.put("versionFrom", versionFrom);
        params.put("versionTo", versionTo);

        PreparedStatementData ps = queryBuilder.psCheckCrossVersions(refBook);

        return getNamedParameterJdbcTemplate().query(ps.getQueryString(), params, new RowMapper<CheckCrossVersionsResult>() {
            @Override
            public CheckCrossVersionsResult mapRow(ResultSet rs, int rowNum) throws SQLException {
                CheckCrossVersionsResult result = new CheckCrossVersionsResult();
                result.setNum(SqlUtils.getInteger(rs, "NUM"));
                result.setRecordId(SqlUtils.getLong(rs, "ID"));
                result.setVersion(rs.getDate("VERSION"));
                result.setStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs, "STATUS")));
                result.setNextVersion(rs.getDate("NEXTVERSION"));
                if (SqlUtils.getInteger(rs, "NEXTSTATUS") != null)
                    result.setNextStatus(VersionedObjectStatus.getStatusById(SqlUtils.getInteger(rs, "NEXTSTATUS")));
                else
                    result.setNextStatus(null);
                result.setResult(CrossResult.getResultById(SqlUtils.getInteger(rs, "RESULT")));
                return result;
            }
        });
    }

    @Override
    public List<Pair<Date, Date>> isVersionUsedLikeParent(RefBook refBook, Long parentId, Date versionFrom) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("versionFrom", versionFrom);
        parameters.put("parentId", parentId);

        PreparedStatementData ps = queryBuilder.psVersionUsedLikeParent(refBook);
        return getNamedParameterJdbcTemplate().query(ps.getQueryString(), parameters,
                new RowMapper<Pair<Date, Date>>() {
                    @Override
                    public Pair<Date, Date> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<Date, Date>(rs.getDate("version"), rs.getDate("versionEnd"));
                    }
                });
    }

    @Override
    public Date getNextVersion(RefBook refBook, Date version, String filter) {
        PreparedStatementData ps = queryBuilder.psGetNextVersion(refBook, version, filter);

        try {
            List<Date> versions = getNamedParameterJdbcTemplate().query(ps.getQuery().toString(), ps.getNamedParams(),
                    new RowMapper<Date>() {
                        @Override
                        public Date mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getDate("version");
                        }
                    });
            return versions.get(0);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String GET_NEXT_RECORD_VERSION =
            "with nextVersion as (select r.id, r.record_id, r.status, r.version from %1$s r where r.record_id = :recordId and r.status != -1 and r.version  = \n" +
            "\t(select min(version) from %1$s where record_id=r.record_id and status=0 and version > :versionFrom)),\n" +
            "minNextVersion as (select r.record_id, min(r.version) version from %1$s r, nextVersion nv where r.version > nv.version and r.record_id= nv.record_id and r.status != -1 group by r.record_id),\n" +
            "nextVersionEnd as (select mnv.record_id, mnv.version, r.status from minNextVersion mnv, %1$s r where mnv.version=r.version and mnv.record_id=r.record_id and r.status != -1)\n" +
            "select nv.id as %2$s, nv.version as versionStart, nve.version - interval '1' day as versionEnd,\n" +
            "case when (nve.status = 2) then 1 else 0 end as endIsFake \n" +
            "from nextVersion nv \n" +
            "left join nextVersionEnd nve on (nve.record_id= nv.record_id)";

    @Override
    public RefBookRecordVersion getNextVersion(RefBook refBook, Long recordId, Date versionFrom) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("recordId", recordId);
        parameters.addValue("versionFrom", versionFrom);

        String sql = String.format(GET_NEXT_RECORD_VERSION, refBook.getTableName(), RefBook.RECORD_ID_ALIAS);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql,
                    parameters,
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void createFakeRecordVersion(RefBook refBook, Long recordId, Date version) {

        PreparedStatementData ps = queryBuilder.psCreateFakeRecordVersion(refBook, recordId, version);
        getNamedParameterJdbcTemplate().update(ps.getQueryString(), ps.getNamedParams());
    }

    @Override
    public List<Long> createRecordVersion(final RefBook refBook, final Date version, final VersionedObjectStatus status,
                                          final List<RefBookRecord> records) {

        final List<Long> refBookRecordIds = dbUtils.getNextRefBookRecordIds((long) records.size());
        MapSqlParameterSource[] allParameters = makeParametersForCreateRecordVersion(refBook, version, status, records, refBookRecordIds);

        PreparedStatementData ps = queryBuilder.psCreateRecordVersion(refBook);
        getNamedParameterJdbcTemplate().batchUpdate(ps.getQueryString(), allParameters);
        return refBookRecordIds;
    }

    private MapSqlParameterSource[] makeParametersForCreateRecordVersion(RefBook refBook, Date version, VersionedObjectStatus status,
                                                                         List<RefBookRecord> records, List<Long> refBookRecordIds) {
        MapSqlParameterSource[] allParameters = new MapSqlParameterSource[refBookRecordIds.size()];
        for (int i = 0; i < refBookRecordIds.size(); i++) {
            RefBookRecord record = records.get(i);

            MapSqlParameterSource recordParameters = new MapSqlParameterSource();
            recordParameters.addValue("id", refBookRecordIds.get(i));
            recordParameters.addValue("recordId", record.getRecordId());
            recordParameters.addValue("version", new java.sql.Date(version.getTime()));
            recordParameters.addValue("status", status.getId());

            for (RefBookAttribute attribute : refBook.getAttributes()) {
                Object recordValue = getValueAsObjectFromRecordByAttribute(record, attribute);
                recordParameters.addValue(attribute.getAlias(), recordValue);
            }
            allParameters[i] = recordParameters;
        }
        return allParameters;
    }

    private Object getValueAsObjectFromRecordByAttribute(RefBookRecord record, RefBookAttribute attribute) {
        RefBookValue recordAttributeValue = record.getValues().get(attribute.getAlias());
        if (recordAttributeValue == null) {
            return null;
        } else {
            switch (attribute.getAttributeType()) {
                case STRING:
                    return recordAttributeValue.getStringValue();
                case NUMBER:
                    return recordAttributeValue.getNumberValue();
                case DATE:
                    return recordAttributeValue.getDateValue();
                case REFERENCE:
                    return recordAttributeValue.getReferenceValue();
            }
        }
        return null;
    }

}
