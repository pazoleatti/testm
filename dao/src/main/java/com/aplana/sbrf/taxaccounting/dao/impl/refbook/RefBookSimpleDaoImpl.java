package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.components.RefBookSimpleQueryBuilderComponent;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Имплементация DAO для RefBookSimpleDataProvider
 */
@Repository
public class RefBookSimpleDaoImpl extends AbstractDao implements RefBookSimpleDao {

    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookSimpleQueryBuilderComponent queryBuilder;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(String tableName, Long refBookId, Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);
        PreparedStatementData ps = queryBuilder.getRecordsQuery(refBook, null, null, version, sortAttribute, filter, pagingParams, isSortAscending, false);

        List<Map<String, RefBookValue>> records = refBookDao.getRecordsData(ps, refBook);

        ps = queryBuilder.getRecordsQuery(refBook, null, null, version, sortAttribute, filter, null, isSortAscending, false);
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(String tableName, Long refBookId, Date version,
                                                                      Long parentRecordId, PagingParams pagingParams,
                                                                      String filter, RefBookAttribute sortAttribute) {
        if (parentRecordId == null && filter == null) {
            String fullFilter = RefBook.RECORD_PARENT_ID_ALIAS + " is null";
            return getRecords(tableName, refBookId, version, pagingParams, fullFilter, sortAttribute, true);
        } else {
            return getChildrenRecords(tableName, refBookId, version, parentRecordId, pagingParams, filter, sortAttribute, true);
        }
    }

    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(String tableName, Long refBookId, Date version,
                                                                      Long parentRecordId, PagingParams pagingParams,
                                                                      String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = queryBuilder.getChildrenQuery(refBook, parentRecordId, version, sortAttribute, filter, pagingParams, isSortAscending);
        List<Map<String, RefBookValue>> records = refBookDao.getRecordsWithHasChild(ps, refBook);

        ps = queryBuilder.getChildrenQuery(refBook, parentRecordId, version, sortAttribute, filter, null, isSortAscending);

        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(refBookDao.getRecordsCount(ps));
        return result;
    }

    @Override
    public Long getRowNum(@NotNull Long refBookId, Date version, Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = queryBuilder.getRecordsQuery(refBook, recordId, null, version, sortAttribute, filter, null, isSortAscending, false);

        return refBookDao.getRowNum(ps, recordId);
    }

    @Override
    public List<Long> getUniqueRecordIds(Long refBookId, String tableName, Date version, String filter) {
        RefBook refBook = refBookDao.get(refBookId);

        PreparedStatementData ps = queryBuilder.getRecordsQuery(refBook, null, null, version, null, filter, null, false, true);

        return getJdbcTemplate().queryForList(ps.getQuery().toString(), ps.getParams().toArray(), Long.class);
    }

    @Override
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

    @Override
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

    @Override
    public List<Date> getVersions(String tableName, Date startDate, Date endDate) {
        String sql = String.format("SELECT version FROM %s " +
                "where version >= ? and version <= ? GROUP BY version", tableName);
        return getJdbcTemplate().queryForList(sql, new Object[]{startDate, endDate}, new int[]{Types.DATE, Types.DATE}, Date.class);
    }

    @Override
    public int getRecordVersionsCount(String tableName, Long uniqueRecordId) {
        String sql = "select count(*) as cnt from %s where STATUS=" + VersionedObjectStatus.NORMAL.getId() + " and RECORD_ID=(select RECORD_ID from %s where ID=?)";
        return getJdbcTemplate().queryForInt(String.format(sql, tableName, tableName),
                uniqueRecordId);
    }
}
