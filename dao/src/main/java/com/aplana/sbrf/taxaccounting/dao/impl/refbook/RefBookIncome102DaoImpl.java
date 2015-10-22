package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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
public class RefBookIncome102DaoImpl extends AbstractDao implements RefBookIncome102Dao {

	private static final Log LOG = LogFactory.getLog(RefBookIncome102DaoImpl.class);

    @Autowired
    private RefBookDao refBookDao;
	@Autowired
	private RefBookIncome101Dao income101Dao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
		return refBookDao.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return getRecords(pagingParams, filter, sortAttribute, true);
    }

    private final static String INCOME_102_FILTER_BY_DEPARTMENT = "select res.id record_id from INCOME_102 res\n" +
            "join REF_BOOK_RECORD rbr on res.ACCOUNT_PERIOD_ID = rbr.ID\n" +
            "join REF_BOOK_VALUE rbv on rbr.id = rbv.RECORD_ID\n" +
            "  where attribute_id = 1073 and %s";
    @Override
    public List<Long> getUniqueRecordIds(String filter) {
        if (filter.toUpperCase().contains("DEPARTMENT_ID")){
            String filterDep = filter.toUpperCase().replace("DEPARTMENT_ID", "REFERENCE_VALUE");
            return getJdbcTemplate().query(String.format(INCOME_102_FILTER_BY_DEPARTMENT, filterDep),
                    new RowMapper<Long>() {
                        @Override
                        public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return SqlUtils.getLong(rs, RefBook.RECORD_ID_ALIAS);
                        }
                    });
        } else
            return refBookDao.getUniqueRecordIds(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public int getRecordsCount(String filter) {
        return refBookDao.getRecordsCount(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return getJdbcTemplate().queryForObject(String.format("select ID as " + RefBook.RECORD_ID_ALIAS + ", OPU_CODE," +
                        " TOTAL_SUM, ITEM_NAME, ACCOUNT_PERIOD_ID" +
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

        if (records == null || records.isEmpty()) {
            return;
        }

        final RefBook refBook = refBookDao.get(REF_BOOK_ID);

        // Удаление записей с совпадающими ACCOUNT_PERIOD_ID
        Set<Long> delList = new HashSet<Long>();

        for (Map<String, RefBookValue> record : records) {
            // проверка обязательности заполнения записей справочника
            List<String> errors= RefBookUtils.checkFillRequiredRefBookAtributes(refBook.getAttributes(), record);
            if (!errors.isEmpty()){
                throw new DaoException("Поля " + errors.toString() + "являются обязательными для заполнения");
            }

            long accountPeriodId = record.get("ACCOUNT_PERIOD_ID").getReferenceValue();
            delList.add(accountPeriodId);
        }

        List<Object[]> delObjs = new LinkedList<Object[]>();
        for (Long id : delList) {
            delObjs.add(new Object[]{id});
        }

        getJdbcTemplate().batchUpdate(String.format("delete from %s where ACCOUNT_PERIOD_ID = ?", TABLE_NAME), delObjs,
                new int[]{Types.NUMERIC, Types.NUMERIC});



        // Добавление записей
        getJdbcTemplate().batchUpdate(
                String.format("insert into %s (" +
                        " ID," +
                        " OPU_CODE," +
                        " TOTAL_SUM," +
                        " ITEM_NAME," +
                        " ACCOUNT_PERIOD_ID)" +
                        " values (seq_income_102.nextval,?,?,?,?)", TABLE_NAME),
                new BatchPreparedStatementSetter() {

                    private Iterator<Map<String, RefBookValue>> iterator = records.iterator();

                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        Map<String, RefBookValue> map = iterator.next();

                        RefBookValue val = map.get("OPU_CODE");
                        ps.setString(1, val.getStringValue());

                        val = map.get("TOTAL_SUM");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(2, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("TOTAL_SUM").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(2, Types.NUMERIC);
                        }

                        val = map.get("ITEM_NAME");
                        if (val != null && val.getStringValue() != null) {
                            ps.setString(3, val.getStringValue());
                        } else {
                            ps.setNull(3, Types.VARCHAR);
                        }

                        ps.setLong(4, map.get("ACCOUNT_PERIOD_ID").getReferenceValue());
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

    private final static String INCOME_102_GET_SEPARATE_VALUE =
            "SELECT\n" +
                    "  (SELECT\n" +
                    "  number_value nv\n" +
                    "   FROM REF_BOOK_VALUE\n" +
                    "   WHERE attribute_id = 1071 AND record_id = (SELECT\n" +
                    "                                                INCOME_102.ACCOUNT_PERIOD_ID\n" +
                    "                                              FROM INCOME_102\n" +
                    "                                              WHERE id = :incomeId)) || ' ' || v2.STRING_VALUE rp_name\n" +
                    "FROM INCOME_102 res\n" +
                    "  JOIN REF_BOOK_VALUE rbv ON res.ACCOUNT_PERIOD_ID = rbv.RECORD_ID\n" +
                    "  JOIN REF_BOOK_ATTRIBUTE rba ON rbv.ATTRIBUTE_ID = rba.ID\n" +
                    "  JOIN ref_book_value v2 ON v2.record_id = rbv.reference_value\n" +
                    "WHERE res.id = :incomeId AND rba.REF_BOOK_ID = :refBookId AND rbv.ATTRIBUTE_ID = 1072 AND v2.ATTRIBUTE_ID = 1062";

    @Override
    public String getPeriodNameFromRefBook(final long recordId) {
        try {
            return getNamedParameterJdbcTemplate().queryForObject(INCOME_102_GET_SEPARATE_VALUE,
                    new HashMap<String, Object>(){{put("incomeId",recordId);put("refBookId", INCOME_102_AP_REF_BOOK_ID);}},
                    String.class);
        } catch (EmptyResultDataAccessException e){
            return "";
        } catch (DataAccessException e){
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }
}
