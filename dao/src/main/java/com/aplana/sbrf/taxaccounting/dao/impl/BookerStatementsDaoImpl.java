package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BookerStatementsDao;
import com.aplana.sbrf.taxaccounting.model.Income101;
import com.aplana.sbrf.taxaccounting.model.Income102;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
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
    public void create101(final List<Income101> list, final Integer periodID) {
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
                            ps.setInt(1, periodID);
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
    public void create102(final List<Income102> list, final Integer periodID) {
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
                            ps.setInt(1, periodID);
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
    public void delete101(Integer periodID) {
        getJdbcTemplate().update(
                "delete from INCOME_101  where REPORT_PERIOD_ID = ?",
                periodID
        );
    }

    @Override
    public void delete102(Integer periodID) {
        getJdbcTemplate().update(
                "delete from INCOME_102  where REPORT_PERIOD_ID = ?",
                periodID
        );
    }

}
