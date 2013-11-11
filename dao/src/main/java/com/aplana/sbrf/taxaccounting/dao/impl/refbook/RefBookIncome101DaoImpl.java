package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * User: ekuvshinov
 */
@Repository
public class RefBookIncome101DaoImpl extends AbstractDao implements RefBookIncome101Dao {

	private static final String TABLE_NAME = "INCOME_101";

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

	@Autowired
	private RefBookUtils refBookUtils;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Integer reportPeriodId, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute) {
		if (filter == null || filter.isEmpty()) {
			filter = " REPORT_PERIOD_ID = " + reportPeriodId;
		} else {
			filter += " AND REPORT_PERIOD_ID = " + reportPeriodId;
		}
		return refBookUtils.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return getJdbcTemplate().queryForObject("select * from " + TABLE_NAME + " where id = ?",
                new RefBookValueMapper(refBookDao.get(REF_BOOK_ID)),
                recordId);
    }

    @Override
    public List<ReportPeriod> gerReportPeriods() {
        String sql = "select distinct report_period_id from " + TABLE_NAME;
        return getJdbcTemplate().query(sql, new RowMapper<ReportPeriod>() {
            @Override
            public ReportPeriod mapRow(ResultSet rs, int rowNum) throws SQLException {
                return reportPeriodDao.get(rs.getInt(1));
            }
        });
    }

    @Override
    public void updateRecords(final List<Map<String, RefBookValue>> records) {
        final RefBook refBook = refBookDao.get(REF_BOOK_ID);

        if (records == null || records.isEmpty()) {
            return;
        }

        // Удаление записей с совпадающими REPORT_PERIOD_ID и DEPARTMENT_ID
        Set<Pair<Long, Long>> delList = new HashSet<Pair<Long, Long>>();

        for (Map<String, RefBookValue> record : records) {
            // проверка обязательности заполнения записей справочника
            List<String> errors= refBookUtils.checkFillRequiredRefBookAtributes(refBook.getAttributes(), record);
            if (errors.size() > 0){
                throw new DaoException("Поля " + errors.toString() + "являются обязательными для заполнения");
            }

            long repId = record.get("REPORT_PERIOD_ID").getNumberValue().longValue();
            long depId = record.get("DEPARTMENT_ID").getReferenceValue().longValue();
            delList.add(new Pair<Long, Long>(repId, depId));
        }

        List<Object[]> delObjs = new LinkedList<Object[]>();
        for (Pair<Long, Long> pair : delList) {
            delObjs.add(new Object[]{pair.getFirst(), pair.getSecond()});
        }

        getJdbcTemplate().batchUpdate("delete from income_101 where report_period_id = ? and department_id = ?", delObjs,
                new int[]{Types.NUMERIC, Types.NUMERIC});

        // Добавление записей
        getJdbcTemplate().batchUpdate(
                "insert into income_101 (" +
                        " ID," +
                        " REPORT_PERIOD_ID," +
                        " ACCOUNT," +
                        " INCOME_DEBET_REMAINS," +
                        " INCOME_CREDIT_REMAINS," +
                        " DEBET_RATE," +
                        " CREDIT_RATE," +
                        " OUTCOME_DEBET_REMAINS," +
                        " OUTCOME_CREDIT_REMAINS," +
                        " ACCOUNT_NAME," +
                        " DEPARTMENT_ID)" +
                        " values (seq_income_101.nextval,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    private Iterator<Map<String, RefBookValue>> iterator = records.iterator();

                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        Map<String, RefBookValue> map = iterator.next();

                        ps.setLong(1, map.get("REPORT_PERIOD_ID").getNumberValue().longValue());

                        RefBookValue val = map.get("ACCOUNT");
                        ps.setString(2, val.getStringValue());

                        val = map.get("INCOME_DEBET_REMAINS");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(3, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("INCOME_DEBET_REMAINS").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(3, Types.NUMERIC);
                        }

                        val = map.get("INCOME_CREDIT_REMAINS");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(4, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("INCOME_CREDIT_REMAINS").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(4, Types.NUMERIC);
                        }

                        val = map.get("DEBET_RATE");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(5, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("DEBET_RATE").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(5, Types.NUMERIC);
                        }

                        val = map.get("CREDIT_RATE");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(6, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("CREDIT_RATE").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(6, Types.NUMERIC);
                        }

                        val = map.get("OUTCOME_DEBET_REMAINS");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(7, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("OUTCOME_DEBET_REMAINS").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(7, Types.NUMERIC);
                        }

                        val = map.get("OUTCOME_CREDIT_REMAINS");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(8, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("OUTCOME_CREDIT_REMAINS").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(8, Types.NUMERIC);
                        }

                        val = map.get("ACCOUNT_NAME");
                        if (val != null && val.getStringValue() != null) {
                            ps.setString(9, val.getStringValue());
                        } else {
                            ps.setNull(9, Types.VARCHAR);
                        }

                        ps.setLong(10, map.get("DEPARTMENT_ID").getReferenceValue().longValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return records.size();
                    }
                }
        );
    }
}
