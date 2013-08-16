package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsDao;
import com.aplana.sbrf.taxaccounting.model.Income101;
import com.aplana.sbrf.taxaccounting.model.Income102;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Дао для формы "Загрузка бухгалтерской отчётности из xls"
 *
 * @author Stanislav Yasinskiy
 */
@Repository
public class BookerStatementsDaoImpl extends AbstractDao implements BookerStatementsDao {

    @Override
    public void create101(final List<Income101> list, final Integer reportPeriodId) {
        if (!list.isEmpty()) {
            getJdbcTemplate().batchUpdate(
                    "insert into INCOME_101 (" +
                            " REPORT_PERIOD_ID," +
                            " ACCOUNT," +
                            " INCOME_DEBET_REMAINS," +
                            " INCOME_CREDIT_REMAINS," +
                            " DEBET_RATE," +
                            " CREDIT_RATE," +
                            " OUTCOME_DEBET_REMAINS," +
                            " OUTCOME_CREDIT_REMAINS," +
                            " ACCOUNT_NAME)" +
                            " values (?,?,?,?,?,?,?,?,?)",
                    new BatchPreparedStatementSetter() {

                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            Income101 model = iterator.next();
                            ps.setInt(1, reportPeriodId);
                            ps.setString(2, model.getAccount());
                            ps.setDouble(3, model.getIncomeDebetRemains());
                            ps.setDouble(4, model.getIncomeCreditRemains());
                            ps.setDouble(5, model.getDebetRate());
                            ps.setDouble(6, model.getCreditRate());
                            ps.setDouble(7, model.getOutcomeDebetRemains());
                            ps.setDouble(8, model.getOutcomeCreditRemains());
                            ps.setString(9, model.getAccountName());
                        }

                        @Override
                        public int getBatchSize() {
                            return list.size();
                        }

                        private Iterator<Income101> iterator = list.iterator();
                    }
            );
        }
    }

    @Override
    public void create102(final List<Income102> list, final Integer reportPeriodId) {
        if (!list.isEmpty()) {
            getJdbcTemplate().batchUpdate(
                    "insert into INCOME_102 (" +
                            " REPORT_PERIOD_ID," +
                            " OPU_CODE," +
                            " TOTAL_SUM," +
                            " ITEM_NAME)" +
                            " values (?,?,?,?)",
                    new BatchPreparedStatementSetter() {

                        @Override
                        public void setValues(PreparedStatement ps, int index) throws SQLException {
                            Income102 model = iterator.next();
                            ps.setInt(1, reportPeriodId);
                            ps.setString(2, model.getOpuCode());
                            ps.setDouble(3, model.getTotalSum());
                            ps.setString(4, model.getItemName());
                        }

                        @Override
                        public int getBatchSize() {
                            return list.size();
                        }

                        private Iterator<Income102> iterator = list.iterator();
                    }
            );
        }
    }

    @Override
    public int delete101(Integer reportPeriodId) {
        try {
            return getJdbcTemplate().update("delete from INCOME_101  where REPORT_PERIOD_ID = ?", reportPeriodId);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public int delete102(Integer reportPeriodId) {
        try {
            return getJdbcTemplate().update("delete from INCOME_102  where REPORT_PERIOD_ID = ?", reportPeriodId);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }


    @Override
    public List<Income101> getIncome101(int reportPeriodId) {
        try {
            return getJdbcTemplate().query(
                    "SELECT * FROM income_101 WHERE REPORT_PERIOD_ID= ?",
                    new Object[]{reportPeriodId},
                    new RowMapper<Income101>() {
                        @Override
                        public Income101 mapRow(ResultSet rs, int rowNum) throws SQLException {
                            Income101 income101Data = new Income101();
                            income101Data.setReportPeriodId(rs.getInt("REPORT_PERIOD_ID"));
                            income101Data.setAccount(rs.getString("ACCOUNT"));
                            income101Data.setIncomeDebetRemains(rs.getDouble("INCOME_DEBET_REMAINS"));
                            income101Data.setIncomeCreditRemains(rs.getDouble("INCOME_CREDIT_REMAINS"));
                            income101Data.setDebetRate(rs.getDouble("DEBET_RATE"));
                            income101Data.setCreditRate(rs.getDouble("CREDIT_RATE"));
                            income101Data.setOutcomeCreditRemains(rs.getDouble("OUTCOME_CREDIT_REMAINS"));
                            income101Data.setOutcomeDebetRemains(rs.getDouble("OUTCOME_DEBET_REMAINS"));
                            income101Data.setAccountName(rs.getString("ACCOUNT_NAME"));
                            return income101Data;
                        }
                    }
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Income102> getIncome102(int reportPeriodId) {
        try {
            return getJdbcTemplate().query(
                    "SELECT * FROM income_102 WHERE REPORT_PERIOD_ID= ?",
                    new Object[]{reportPeriodId},
                    new RowMapper<Income102>() {
                        @Override
                        public Income102 mapRow(ResultSet rs, int rowNum) throws SQLException {
                            Income102 income102Data = new Income102();
                            income102Data.setReportPeriodId(rs.getInt("REPORT_PERIOD_ID"));
                            income102Data.setOpuCode(rs.getString("OPU_CODE"));
                            income102Data.setTotalSum(rs.getDouble("TOTAL_SUM"));
                            income102Data.setItemName(rs.getString("ITEM_NAME"));
                            return income102Data;
                        }
                    }
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
