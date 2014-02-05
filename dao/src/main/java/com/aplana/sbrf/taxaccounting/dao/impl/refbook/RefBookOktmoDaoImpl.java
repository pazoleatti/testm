package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookOktmoDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author auldanov on 03.02.14.
 */
@Repository
public class RefBookOktmoDaoImpl extends AbstractDao implements RefBookOktmoDao {

    @Autowired
    private RefBookUtils refBookUtils;

    @Autowired
    private RefBookDao refBookDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        if (filter == null || filter.isEmpty()) {
            filter = " version = " + version;
        } else {
            filter += " AND version = " + version;
        }
        return refBookUtils.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookUtils.getRecordData(REF_BOOK_ID, TABLE_NAME, recordId);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        String sql = String.format("SELECT version FROM %s " +
                "where version >= ? and version <= ? GROUP BY version", TABLE_NAME);
        return getJdbcTemplate().queryForList(sql, new Object[]{startDate, endDate}, new int[]{Types.DATE, Types.DATE}, Date.class);
    }

    @Override
    public void insertRecords(final Date version, final List<Map<String, RefBookValue>> records) {
        String sql = String.format("INSERT INTO %s(id, code, name, parent_id, version, status) VALUES(???, ?, ?, ?, ?, 1)", TABLE_NAME);
        getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, records.get(i).get("CODE").getStringValue());
                ps.setString(2, records.get(i).get("NAME").getStringValue());
                // TODO откуда брать родителя?
                ps.setDate(3, new java.sql.Date(version.getTime()));
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        if (filter == null || filter.isEmpty()) {
            filter = " parent_id = " + parentRecordId;
        } else {
            filter += " AND parent_id = " + parentRecordId;
        }
        return refBookUtils.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, null);
    }

    @Override
    public void deleteAllRecords(Date version) {
        getJdbcTemplate().update(String.format("DELETE FROM %s WHERE version = ?", TABLE_NAME), version);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(Long id, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        if (id == null){
            throw new RuntimeException("Id must be not null");
        }

        String whereClause = String.format(" record_id = (SELECT record_id FORM %s WHERE id = %d)", TABLE_NAME, id);
        return refBookUtils.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, whereClause);
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long id) {
        String sql = "SELECT id, max(version) as versionEnd, min(version) as versionStart" +
                " FROM %s WHERE record_id = (SELECT record_id FORM " + TABLE_NAME + " WHERE id = ? )" +
                " GROUP BY id";

        try {
            return getJdbcTemplate().queryForObject(sql,
                    new Object[]{id},
                    new RefBookUtils.RecordVersionMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int getRecordVersionsCount(Long id) {
        String sql = "SELECT count(id) FROM " + TABLE_NAME + " WHERE record_id = " +
                "(SELECT record_id FORM " + TABLE_NAME + "WHERE id = ? ) "+
                "GROUP BY version";

        return getJdbcTemplate().queryForInt(sql, id);
    }


}
