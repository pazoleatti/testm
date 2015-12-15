package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookUserDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author auldanov
 */
@Repository
class RefBookUserDaoImpl extends AbstractDao implements RefBookUserDao {

    @Autowired
    private RefBookDao refBookDao;

    public static final String TABLE_NAME = RefBookUserDao.TABLE_NAME;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return refBookDao.getUniqueRecordIds(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookDao.getRecordsCount(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        StringBuilder sql = new StringBuilder("SELECT id ");
        sql.append(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", ");
            sql.append(attribute.getAlias());
        }
        sql.append(" FROM "+TABLE_NAME+" WHERE id = :id");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("id", recordId);
        List<Map<String, RefBookValue>> records = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RefBookValueMapper(refBook));
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        if (records.size() == 1) {
            result = records.get(0);
        }
        return result;
    }

    @Override
    public Long getRowNum(Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(REF_BOOK_ID, TABLE_NAME, recordId, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public Map<Long, CheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds) {
        String sql = String.format("select id from "+ TABLE_NAME +" where %s", SqlUtils.transformToSqlInStatement("id", recordIds));
        Map<Long, CheckResult> result = new HashMap<Long, CheckResult>();
        List<Long> existRecords = new ArrayList<Long>();
        try {
            existRecords = getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException ignored) {}
        for (Long recordId : recordIds) {
            if (!existRecords.contains(recordId)) {
                result.put(recordId, CheckResult.NOT_EXISTS);
            }
        }
        return result;
    }
}
