package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.BookerStatementsFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
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
@Transactional
public class RefBookIncome101DaoImpl extends AbstractDao implements RefBookIncome101Dao {

    private static long REF_BOOK_ID = 50L;

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Integer reportPeriodId, PagingParams pagingParams, String filter,
                                                              RefBookAttribute sortAttribute) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);

		String sortColumn = sortAttribute == null ? "id" : sortAttribute.getAlias();

        StringBuilder sql = new StringBuilder("select ");
		// Сортировка
		if (isSupportOver()) {
			sql.append("* from ( select row_number() over (order by '").append(sortColumn).append("') as row_number_over, ");
		}
		sql.append("id, report_period_id, account, income_debet_remains, income_credit_remains, debet_rate, ");
		sql.append("credit_rate, outcome_debet_remains, outcome_credit_remains, account_name, department_id ");
		sql.append("from income_101 ");

        Map<String, Integer> params = new HashMap<String, Integer>();
        sql.append("WHERE report_period_id = :reportPeriodId ");
        params.put("reportPeriodId", reportPeriodId);

        // Фильтрация
        StringBuffer sb = new StringBuffer();
        Filter.getFilterQuery(filter, new BookerStatementsFilterTreeListener(refBook, sb));

        if (sb.length() > 0) {
            sql.append("AND ");
            sql.append(sb.toString());
        }

		if (isSupportOver()) {
			sql.append(')');
			if (pagingParams != null) {
				// Тестовая база не поддерживает такой синтаксис
				sql.append(" WHERE row_number_over between :from and :to ");
				params.put("from", pagingParams.getStartIndex());
				params.put("to", pagingParams.getStartIndex() + pagingParams.getCount());
			}
		}

        List<Map<String, RefBookValue>> records = getNamedParameterJdbcTemplate().query(sql.toString(), params,
                new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
        result.addAll(records);

        result.setTotalRecordCount(getNamedParameterJdbcTemplate().queryForInt("select count(*) from (" + sql.toString() + ")", params));
        return result;
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return getJdbcTemplate().queryForObject("select * from income_101 where id = ?",
                new RefBookValueMapper(refBookDao.get(REF_BOOK_ID)),
                recordId);
    }

    @Override
    public List<ReportPeriod> gerReportPeriods() {
        String sql = "select distinct report_period_id from income_101";
        return getJdbcTemplate().query(sql, new RowMapper<ReportPeriod>() {
            @Override
            public ReportPeriod mapRow(ResultSet rs, int rowNum) throws SQLException {
                return reportPeriodDao.get(rs.getInt(1));
            }
        });
    }

    @Override
    public void updateRecords(final List<Map<String, RefBookValue>> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        // Удаление записей с совпадающими REPORT_PERIOD_ID и DEPARTMENT_ID
        Set<Pair<Long, Long>> delList = new HashSet<Pair<Long, Long>>();

        for (Map<String, RefBookValue> record : records) {
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

        final RefBook refBook = refBookDao.get(REF_BOOK_ID);

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
