package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
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
 * User: ekuvshinov
 */
@Repository
public class RefBookIncome101DaoImpl extends AbstractDao implements RefBookIncome101Dao {

	private static final Log LOG = LogFactory.getLog(RefBookIncome101DaoImpl.class);

    @Autowired
    private RefBookDao refBookDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(pagingParams, filter, sortAttribute, true);
    }


    private final static String INCOME_101_FILTER_BY_DEPARTMENT = "select res.id record_id from INCOME_101 res\n" +
            "join REF_BOOK_RECORD rbr on res.ACCOUNT_PERIOD_ID = rbr.ID\n" +
            "join REF_BOOK_VALUE rbv on rbr.id = rbv.RECORD_ID\n" +
            "  where attribute_id = 1073 and %s";
    @Override
    public List<Long> getUniqueRecordIds(String filter) {
        //Грязь связанная с изменением структуры хранения БО
        //http://conf.aplana.com/pages/viewpage.action?pageId=9584598
        if (filter.toUpperCase().contains("DEPARTMENT_ID")){
            String filterDep = filter.toUpperCase().replace("DEPARTMENT_ID", "REFERENCE_VALUE");
            return getJdbcTemplate().query(String.format(INCOME_101_FILTER_BY_DEPARTMENT, filterDep),
                    new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return SqlUtils.getLong(rs, RefBook.RECORD_ID_ALIAS);
                }
            });
        }else
            return refBookDao.getUniqueRecordIds(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public int getRecordsCount(String filter) {
        return refBookDao.getRecordsCount(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        try {
            return getJdbcTemplate().queryForObject("select ID as " + RefBook.RECORD_ID_ALIAS + ", ACCOUNT, " +
                            "INCOME_DEBET_REMAINS, INCOME_CREDIT_REMAINS, DEBET_RATE, CREDIT_RATE, OUTCOME_DEBET_REMAINS, " +
                            "OUTCOME_CREDIT_REMAINS, ACCOUNT_NAME, ACCOUNT_PERIOD_ID " +
                            "from " + TABLE_NAME + " where id = ?",
                    new RefBookValueMapper(refBookDao.get(REF_BOOK_ID)),
                    recordId);
        } catch (EmptyResultDataAccessException e) {
            return new HashMap<String, RefBookValue>(0);
        } catch (DataAccessException e) {
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

	@Override
	public List<Date> getVersions(Date startDate, Date endDate) {
		return getVersions(TABLE_NAME, startDate, endDate);
	}

    @Override
    public List<Date> getVersions(String tableName, Date startDate, Date endDate) {
        return null;
    }

    @Override
    public void updateRecords(final List<Map<String, RefBookValue>> records) {

        if (records == null || records.isEmpty()) {
            return;
        }

        final RefBook refBook = refBookDao.get(REF_BOOK_ID);

        // Удаление записей с совпадающим ACCOUNT_PERIOD_ID
        Set<Long> delList = new HashSet<Long>();

        for (Map<String, RefBookValue> record : records) {
            // проверка обязательности заполнения записей справочника
            List<String> errors= RefBookUtils.checkFillRequiredRefBookAtributes(refBook.getAttributes(), record);
            if (errors.size() > 0){
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
                        " ACCOUNT," +
                        " INCOME_DEBET_REMAINS," +
                        " INCOME_CREDIT_REMAINS," +
                        " DEBET_RATE," +
                        " CREDIT_RATE," +
                        " OUTCOME_DEBET_REMAINS," +
                        " OUTCOME_CREDIT_REMAINS," +
                        " ACCOUNT_NAME," +
                        " ACCOUNT_PERIOD_ID)" +
                        " values (seq_income_101.nextval,?,?,?,?,?,?,?,?,?)", TABLE_NAME),
                new BatchPreparedStatementSetter() {

                    private Iterator<Map<String, RefBookValue>> iterator = records.iterator();

                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        Map<String, RefBookValue> map = iterator.next();

                        RefBookValue val = map.get("ACCOUNT");
                        ps.setString(1, val.getStringValue());

                        val = map.get("INCOME_DEBET_REMAINS");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(2, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("INCOME_DEBET_REMAINS").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(2, Types.NUMERIC);
                        }

                        val = map.get("INCOME_CREDIT_REMAINS");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(3, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("INCOME_CREDIT_REMAINS").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(3, Types.NUMERIC);
                        }

                        val = map.get("DEBET_RATE");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(4, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("DEBET_RATE").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(4, Types.NUMERIC);
                        }

                        val = map.get("CREDIT_RATE");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(5, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("CREDIT_RATE").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(5, Types.NUMERIC);
                        }

                        val = map.get("OUTCOME_DEBET_REMAINS");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(6, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("OUTCOME_DEBET_REMAINS").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(6, Types.NUMERIC);
                        }

                        val = map.get("OUTCOME_CREDIT_REMAINS");
                        if (val != null && val.getNumberValue() != null) {
                            ps.setDouble(7, BigDecimal.valueOf(val.getNumberValue().doubleValue())
                                    .setScale(refBook.getAttribute("OUTCOME_CREDIT_REMAINS").getPrecision(),
                                            RoundingMode.HALF_UP).doubleValue());
                        } else {
                            ps.setNull(7, Types.NUMERIC);
                        }

                        val = map.get("ACCOUNT_NAME");
                        if (val != null && val.getStringValue() != null) {
                            ps.setString(8, val.getStringValue());
                        } else {
                            ps.setNull(8, Types.VARCHAR);
                        }

                        ps.setLong(9, map.get("ACCOUNT_PERIOD_ID").getReferenceValue());
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
		getJdbcTemplate().update("delete from income_101 where " + SqlUtils.transformToSqlInStatement("id", uniqueRecordIds));
	}

    private final static String INCOME_101_GET_SEPARATE_VALUE =
            "select (select number_value nv from REF_BOOK_VALUE where attribute_id = 1071 and record_id = (select INCOME_101.ACCOUNT_PERIOD_ID from INCOME_101 where id = :incomeId)) || ' ' || v2.STRING_VALUE rp_name \n" +
                    "from INCOME_101 res\n" +
                    "join REF_BOOK_VALUE rbv on res.ACCOUNT_PERIOD_ID = rbv.RECORD_ID\n" +
                    "join REF_BOOK_ATTRIBUTE rba on rbv.ATTRIBUTE_ID = rba.ID\n" +
                    "join ref_book_value v2 on v2.record_id=rbv.reference_value\n" +
                    "  where res.id = :incomeId and rba.REF_BOOK_ID = :refBookId and rbv.ATTRIBUTE_ID = 1072 and v2.ATTRIBUTE_ID = 1062";

    @Override
    public String getPeriodNameFromRefBook(final long recordId) {
        try {
            return getNamedParameterJdbcTemplate().queryForObject(INCOME_101_GET_SEPARATE_VALUE,
                    new HashMap<String, Object>(){{put("incomeId",recordId);put("refBookId", INCOME_101_AP_REF_BOOK_ID);}},
                    String.class);
        } catch (EmptyResultDataAccessException e){
            return "";
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }
}
