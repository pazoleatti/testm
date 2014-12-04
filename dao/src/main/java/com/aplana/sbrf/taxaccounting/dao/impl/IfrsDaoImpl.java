package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.IfrsDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;

/**
 * Интерфейс для работы с отчетностями МСФО
 * @author lhaziev
 *
 */

@Repository
@Transactional(readOnly=true)
public class IfrsDaoImpl extends AbstractDao implements IfrsDao {

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    private class IfrsDataMapper implements RowMapper<IfrsData> {
        @Override
        public IfrsData mapRow(ResultSet rs, int index) throws SQLException {
            IfrsData ifrsData = new IfrsData();
            ifrsData.setReportPeriodId(SqlUtils.getInteger(rs, "REPORT_PERIOD_ID"));
            ifrsData.setBlobDataId(rs.getString("BLOB_DATA_ID"));
            ifrsData.setYear(SqlUtils.getInteger(rs, "YEAR"));
            ifrsData.setPeriodName(rs.getString("NAME"));
            return ifrsData;
        }
    }

    private class IfrsDataSearchResultMapper implements RowMapper<IfrsDataSearchResultItem> {
        @Override
        public IfrsDataSearchResultItem mapRow(ResultSet rs, int index) throws SQLException {
            IfrsDataSearchResultItem ifrsData = new IfrsDataSearchResultItem();
            ifrsData.setReportPeriodId(SqlUtils.getInteger(rs, "REPORT_PERIOD_ID"));
            ifrsData.setBlobDataId(rs.getString("BLOB_DATA_ID"));
            ifrsData.setYear(SqlUtils.getInteger(rs, "YEAR"));
            ifrsData.setPeriodName(rs.getString("NAME"));
            if (isSupportOver()) ifrsData.setCount(rs.getInt("cnt"));
            return ifrsData;
        }
    }

    @Override
    public void create(final Integer reportPeriodId) {
        try{
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("INSERT INTO IFRS_DATA (REPORT_PERIOD_ID) VALUES (?)");
            ps.addParam(reportPeriodId);
            getJdbcTemplate().update(ps.getQuery().toString(), ps.getParams().toArray());
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось записать данные." + e.toString());
        }
    }

    @Override
    public void update(Integer reportPeriodId, String uuid) {
        try {
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("update ifrs_data set blob_data_id = ? where report_period_id = ?");
            ps.addParam(uuid);
            ps.addParam(reportPeriodId);
            int count = getJdbcTemplate().update(ps.getQuery().toString(), ps.getParams().toArray());
            if (count == 0) {
                throw new DaoException("Не удалось обновить отчетность для МСФО с REPORT_PERIOD_ID = %d, так как она не существует.", reportPeriodId);
            }
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось обновить данные." + e.toString());
        }
    }

    @Override
    public IfrsData get(Integer reportPeriodId) {
        try{
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("select ifrs.report_period_id, ifrs.blob_data_id, rp.name, tp.year " +
                            "from ifrs_data ifrs " +
                            "join report_period rp on rp.id = ifrs.report_period_id " +
                            "join tax_period tp on tp.id=rp.tax_period_id " +
                            "where report_period_id = ?");
            ps.addParam(reportPeriodId);
            return getJdbcTemplate().queryForObject(ps.getQuery().toString(), ps.getParams().toArray(), new IfrsDataMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось получить данные." + e.toString());
        }
    }

    @Override
    public PagingResult<IfrsDataSearchResultItem> findByReportPeriod(List<Integer> reportPeriodIds, PagingParams pagingParams) {
        try{
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("select ordDat.* from (" +
                                "select ifrs.report_period_id, ifrs.blob_data_id, rp.name, tp.year, rownum as rn ");
            if (isSupportOver())
                ps.appendQuery(", count(*) over() cnt ");

            ps.appendQuery("from ifrs_data ifrs  " +
                                "join report_period rp on rp.id = ifrs.report_period_id " +
                                "join tax_period tp on tp.id=rp.tax_period_id ");
            if (reportPeriodIds != null && !reportPeriodIds.isEmpty()) {
                ps.appendQuery("where " + SqlUtils.transformToSqlInStatement("ifrs.report_period_id", reportPeriodIds));
            }
            if (pagingParams != null) {
                ps.appendQuery("order by tp.year " +
                        ") ordDat where orddat.rn between ? and ?");
                ps.addParam(pagingParams.getStartIndex() + 1);
                ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
            } else {
                ps.appendQuery(") ordDat");
            }

            List<IfrsDataSearchResultItem> records = getJdbcTemplate().query(ps.getQuery().toString(),
                                        ps.getParams().toArray(),
                                        new IfrsDataSearchResultMapper());
            return new PagingResult<IfrsDataSearchResultItem>(records, (records.isEmpty()? 0:records.get(0).getCount()));
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось получить данные." + e.toString());
        }
    }

}
