package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome102Dao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Repository
public class RefBookIncome102DaoImpl extends AbstractDao implements RefBookIncome102Dao {

    @Autowired
    private RefBookDao refBookDao;

	@Autowired
	private RefBookUtils refBookUtils;

	@Autowired
	private RefBookIncome101Dao income101Dao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Integer reportPeriodId, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
		if (filter == null || filter.isEmpty()) {
			filter = " REPORT_PERIOD_ID = " + reportPeriodId;
		} else {
			filter += " AND REPORT_PERIOD_ID = " + reportPeriodId;
		}
		return refBookUtils.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Integer reportPeriodId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return getRecords(reportPeriodId, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Long> getUniqueRecordIds(Integer reportPeriodId, String filter) {
        if (filter == null || filter.isEmpty()) {
            filter = " REPORT_PERIOD_ID = " + reportPeriodId;
        } else {
            filter += " AND REPORT_PERIOD_ID = " + reportPeriodId;
        }
        return refBookUtils.getUniqueRecordIds(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return getJdbcTemplate().queryForObject(String.format("select ID as "+RefBook.RECORD_ID_ALIAS+", REPORT_PERIOD_ID, OPU_CODE, TOTAL_SUM, ITEM_NAME, DEPARTMENT_ID" +
                " from %s where id = ?", TABLE_NAME),
                new RefBookValueMapper(refBookDao.get(REF_BOOK_ID)),
                recordId);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
		return income101Dao.getVersions(TABLE_NAME, startDate, endDate);
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

        getJdbcTemplate().batchUpdate(String.format("delete from %s where report_period_id = ? and department_id = ?", TABLE_NAME), delObjs,
                new int[]{Types.NUMERIC, Types.NUMERIC});



        // Добавление записей
        getJdbcTemplate().batchUpdate(
                String.format("insert into %s (" +
                        " ID," +
                        " REPORT_PERIOD_ID," +
                        " OPU_CODE," +
                        " TOTAL_SUM," +
                        " ITEM_NAME," +
                        " DEPARTMENT_ID)" +
                        " values (seq_income_102.nextval,?,?,?,?,?)", TABLE_NAME),
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

	@Override
	public void deleteRecords(List<Long> uniqueRecordIds) {
		getJdbcTemplate().update("delete from income_102 where " + SqlUtils.transformToSqlInStatement("id", uniqueRecordIds));
	}
}
