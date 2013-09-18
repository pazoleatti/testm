package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.BookerStatementsFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome102Dao;
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
 * @author Dmitriy Levykin
 */
@Repository
@Transactional
public class RefBookIncome102DaoImpl extends AbstractDao implements RefBookIncome102Dao {

    private static long REF_BOOK_ID = 52L;

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
		sql.append("id, report_period_id, department_id, opu_code, total_sum, item_name ");
		sql.append("from income_102 ");

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
		PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
		result.setTotalCount(getNamedParameterJdbcTemplate().queryForInt("select count(*) from (" + sql.toString() + ")", params));
		return result;
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return getJdbcTemplate().queryForObject("select * from income_102 where id = ?",
                new RefBookValueMapper(refBookDao.get(REF_BOOK_ID)),
                recordId);
    }

    @Override
    public List<ReportPeriod> gerReportPeriods() {
        String sql = "select distinct report_period_id from income_102";
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

        getJdbcTemplate().batchUpdate("delete from income_102 where report_period_id = ? and department_id = ?", delObjs,
                new int[]{Types.NUMERIC, Types.NUMERIC});

        final RefBook refBook = refBookDao.get(REF_BOOK_ID);

        // Добавление записей
        getJdbcTemplate().batchUpdate(
                "insert into income_102 (" +
                        " ID," +
                        " REPORT_PERIOD_ID," +
                        " OPU_CODE," +
                        " TOTAL_SUM," +
                        " ITEM_NAME," +
                        " DEPARTMENT_ID)" +
                        " values (seq_income_102.nextval,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    private Iterator<Map<String, RefBookValue>> iterator = records.iterator();

                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        Map<String, RefBookValue> map = iterator.next();

                        ps.setLong(1, map.get("REPORT_PERIOD_ID").getNumberValue().longValue());

                        RefBookValue val = map.get("OPU_CODE");
                        ps.setString(2, val.getStringValue());

                        val = map.get("TOTAL_SUM");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(3, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("TOTAL_SUM").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(3, Types.NUMERIC);
                        }

                        val = map.get("ITEM_NAME");
                        if (val != null && val.getStringValue() != null) {
                            ps.setString(4, val.getStringValue());
                        } else {
                            ps.setNull(4, Types.VARCHAR);
                        }

                        ps.setLong(5, map.get("DEPARTMENT_ID").getReferenceValue().longValue());
                    }

                    @Override
                    public int getBatchSize() {
                        return records.size();
                    }
                }
        );
    }
}
