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
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
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

    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookSimpleQueryBuilderComponent queryBuilder;

    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);
        PreparedStatementData ps = queryBuilder.getRecordsQuery(refBook, null, null, version, sortAttribute, filter, pagingParams, isSortAscending, false);

        List<Map<String, RefBookValue>> records = refBookDao.getRecordsData(ps, refBook);

        ps = queryBuilder.getRecordsQuery(refBook, null, null, version, sortAttribute, filter, null, isSortAscending, false);
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

        PreparedStatementData ps = queryBuilder.getChildrenRecordsQuery(refBook, parentRecordId, version, sortAttribute, filter, pagingParams, isSortAscending);
        List<Map<String, RefBookValue>> records = refBookDao.getRecordsWithHasChild(ps, refBook);

        ps = queryBuilder.getChildrenRecordsQuery(refBook, parentRecordId, version, sortAttribute, filter, null, isSortAscending);

        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    public Long getRowNum(@NotNull Long refBookId, Date version, Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = queryBuilder.getRecordsQuery(refBook, recordId, null, version, sortAttribute, filter, null, isSortAscending, false);

        return refBookDao.getRowNum(ps, recordId);
    }

    public List<Long> getUniqueRecordIds(Long refBookId, String tableName, Date version, String filter) {
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = queryBuilder.getRecordsQuery(refBook, null, null, version, null, filter, null, false, true);

        return getJdbcTemplate().queryForList(ps.getQuery().toString(), ps.getParams().toArray(), Long.class);
    }

    public int getRecordsCount(Long refBookId, String tableName, Date version, String filter) {
        RefBook refBook = refBookDao.get(refBookId);
        PreparedStatementData ps = queryBuilder.getRecordsQuery(refBook, null, null, version, null, filter, null, false, false);
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
        PreparedStatementData ps = queryBuilder.getRecordsQuery(refBookClone, recordId, null, null, sortAttribute, filter, pagingParams, false, false);
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
                                                                        List<RefBookAttribute> attributes,
                                                                        List<RefBookRecord> records) {

        List<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>> recordsGroupsUniqueAttributesValues = aggregateUniqueAttributesAndValuesByRecords(attributes, records);

        if (recordsGroupsUniqueAttributesValues.isEmpty()) {
            return new ArrayList<Pair<Long, String>>();
        }

        List<Object> selectParams = new ArrayList<Object>();

        StringBuilder sqlBuilder = queryBuilder.getMatchedRecordsByUniqueAttributes(refBook, uniqueRecordId, records, recordsGroupsUniqueAttributesValues, selectParams);

        List<Pair<Long, String>> result = getJdbcTemplate().query(sqlBuilder.toString(), selectParams.toArray(), new RowMapper<Pair<Long, String>>() {
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
     * @param records    записи
     * @return список записей с группами уникальности списков пар уникальных атрибутов и значений
     */
    List<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>> aggregateUniqueAttributesAndValuesByRecords(List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        // Формируем список для каждой записи из групп уникальных атрибутов с их значениями
        List<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>> listAttributeValues = new ArrayList<Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>>>();

        // для каждой записи
        for (RefBookRecord record : records) {
            Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> attributeValues = new HashMap<Integer, List<Pair<RefBookAttribute, RefBookValue>>>();
            // для каждого атрибута
            for (RefBookAttribute attribute : attributes) {
                // если уникальный
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
            if (!attributeValues.isEmpty()) {
                listAttributeValues.add(attributeValues);
            }
        }
        return listAttributeValues;
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
}
