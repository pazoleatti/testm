package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
//import com.aplana.sbrf.taxaccounting.model.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * User: ekuvshinov
 */
@Repository
public class RefBookDepartmentDaoImpl extends AbstractDao implements RefBookDepartmentDao {

	private static final Log LOG = LogFactory.getLog(RefBookDepartmentDaoImpl.class);

	private static final String TABLE_NAME = "DEPARTMENT";

    @Autowired
    ReportPeriodDao reportPeriodDao;
    @Autowired
    RefBookDao refBookDao;
    @Autowired
    RefBookSimpleDao refBookSimpleDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
		return refBookDao.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public Long getRowNum(Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(REF_BOOK_ID, TABLE_NAME, recordId, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(REF_BOOK_ID, TABLE_NAME, recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> uniqRecordIds) {
        return refBookSimpleDao.getRecordData(refBookDao.get(REF_BOOK_ID), uniqRecordIds);
    }

    private final static String CHECK_UNIQUE_MATCHES_FOR_NON_VERSION =
            "select name from department t where t.is_active = 1 AND %s %s %s";
    @Override
    public List<Pair<String, String>> getMatchedRecordsByUniqueAttributes(Long recordId, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(CHECK_UNIQUE_MATCHES_FOR_NON_VERSION);
        ArrayList<RefBookAttribute> uniqueAttrs = new ArrayList<RefBookAttribute>();
        for (RefBookAttribute attribute : attributes) {
            if (attribute.getUnique() != 0){
                uniqueAttrs.add(attribute);
            }
        }
        //Если уникальных атрибутов нет, то поиск не нужен
        if (uniqueAttrs.isEmpty())
            return new ArrayList<Pair<String, String>>(0);
        String idOddsTag = recordId != null ? "t.id <> " + recordId : "";

        ArrayList<Pair<String, String>> pairList = new ArrayList<Pair<String, String>>();
        String andTag = !uniqueAttrs.isEmpty() && recordId != null ? "AND" : "";
        for (final RefBookAttribute attribute : uniqueAttrs) {
            String querySql;
            Object value = null;
            if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                querySql = String.format(CHECK_UNIQUE_MATCHES_FOR_NON_VERSION,
                        idOddsTag,
                        andTag,
                        "upper(t." + attribute.getAlias() + ") = upper(?)");
            } else {
                querySql = String.format(CHECK_UNIQUE_MATCHES_FOR_NON_VERSION,
                        idOddsTag,
                        andTag,
                        "t." + attribute.getAlias() + " = ?");
            }
            Map<String, RefBookValue> values = records.get(0).getValues();

            if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                value =  values.get(attribute.getAlias()).getStringValue();
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                value = values.get(attribute.getAlias()).getReferenceValue();
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                value = values.get(attribute.getAlias()).getNumberValue();
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                value = values.get(attribute.getAlias()).getDateValue();
            }

            try {
                pairList.addAll(getJdbcTemplate().query(querySql, new Object[]{value},
                        new RowMapper<Pair<String, String>>() {
                            @Override
                            public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                                return new Pair<String, String>(rs.getString("NAME"), attribute.getName());
                            }
                        }));
            } catch (DataAccessException e) {
                throw new DaoException("Ошибка при поиске уникальных значений в справочнике " + TABLE_NAME, e);
            }
        }

        return pairList;
    }

    private static final String UPDATE_DEPARTMENT = "update department t set %s where id = ?";

    @Override
    //@CacheEvict(value = CacheConstants.DEPARTMENT,key = "#uniqueId", beforeInvocation = true)
    public void update(int uniqueId, Map<String, RefBookValue> records,  List<RefBookAttribute> attributes) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(UPDATE_DEPARTMENT);
        StringBuilder sql = new StringBuilder();
        for (RefBookAttribute attribute : attributes) {
            sql.append(String.format("t.%s = ?,",  attribute.getAlias()));

            if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                ps.addParam(records.get(attribute.getAlias()).getStringValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                ps.addParam(records.get(attribute.getAlias()).getReferenceValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                ps.addParam(records.get(attribute.getAlias()).getNumberValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                ps.addParam(records.get(attribute.getAlias()).getDateValue());
            }
        }
        ps.addParam(uniqueId);
        getJdbcTemplate().update(
                String.format(ps.getQuery().toString(), sql.toString().substring(0, sql.toString().length() - 1)),
                ps.getParams().toArray());
    }

    private static final String CREATE_DEPARTMENT = "insert into department (id, %s) values(seq_department.nextval, %s)";
    @Override
    public int create(Map<String, RefBookValue> record, List<RefBookAttribute> attributes) {
        final PreparedStatementData ps = new PreparedStatementData();
        for (RefBookAttribute attribute : attributes) {
            ps.appendQuery(attribute.getAlias() + ",");

            if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                ps.addParam(record.get(attribute.getAlias()).getStringValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                ps.addParam(record.get(attribute.getAlias()).getReferenceValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                ps.addParam(record.get(attribute.getAlias()).getNumberValue());
            }
            if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                ps.addParam(record.get(attribute.getAlias()).getDateValue());
            }
        }
        final String ph = SqlUtils.preparePlaceHolders(attributes.size());
        try {
            PreparedStatementCreator psc = new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement statement = con.prepareStatement(
                            String.format(CREATE_DEPARTMENT, ps.getQuery().toString().substring(0, ps.getQuery().toString().length() - 1), ph),
                            new String[]{"ID"}
                    );
                    for (int i =0; i < ps.getParams().size(); i++)
                        statement.setObject(i+1, ps.getParams().get(i));
                    return statement;
                }
            };
            KeyHolder keyHolder = new GeneratedKeyHolder();
            getJdbcTemplate().update(
                    psc, keyHolder);
            return keyHolder.getKey().intValue();
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void remove(long uniqueId) {
        try {
            getJdbcTemplate().update("DELETE FROM department WHERE id = ?", uniqueId);
        } catch (DataIntegrityViolationException e){
            throw new DaoException("Нарушение ограничения целостности. Возможно обнаружена порожденная запись.", e);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public int getRecordsCount(String filter) {
        return refBookDao.getRecordsCount(REF_BOOK_ID, TABLE_NAME, filter);
    }
    
    private static final String GET_REPORT_PERIOD_NAME = 
            "WITH record_date AS (SELECT\n" +
                    "                  v.record_id AS record_id\n" +
                    "                FROM ref_book b\n" +
                    "                  JOIN ref_book_attribute a ON a.ref_book_id = b.id\n" +
                    "                  JOIN ref_book_value v ON v.attribute_id = a.id\n" +
                    "                WHERE b.id = 8 AND to_char(v.DATE_VALUE, 'DDMM') = to_char(:startDate, 'DDMM') AND a.alias = 'CALENDAR_START_DATE'),\n" +
                    "  record_type AS(SELECT\n" +
                    "                   v.record_id AS record_id\n" +
                    "                 FROM ref_book b\n" +
                    "                   JOIN ref_book_attribute a ON a.ref_book_id = b.id\n" +
                    "                   JOIN ref_book_value v ON v.attribute_id = a.id\n" +
                    "                 WHERE b.id = 8 AND a.alias = :taxCode AND v.NUMBER_VALUE = 1\n" +
                    ")\n" +
                    "SELECT\n" +
                    "  rbv.STRING_VALUE\n" +
                    "FROM ref_book_attribute a\n" +
                    "  JOIN record_date rd ON 1 = 1\n" +
                    "  JOIN record_type rt ON 1 = 1\n" +
                    "  JOIN ref_book_value rbv ON rbv.attribute_id = a.id AND rbv.RECORD_ID = rd.record_id AND rbv.RECORD_ID = rt.record_id\n" +
                    "WHERE a.alias = 'NAME'";

    @Override
    public String getReportPeriodNameByDate(TaxType taxType, Date startDate) {
        try{
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("taxCode", String.valueOf(taxType.getCode()));
            params.put("startDate", startDate);
            return getNamedParameterJdbcTemplate().queryForObject(GET_REPORT_PERIOD_NAME, params, String.class);
        } catch (EmptyResultDataAccessException e){
            return "";
        } catch (DataAccessException e){
            throw new DaoException("", e);
        }
    }

    @Override
    public List<Long> isRecordsExist(List<Long> uniqueRecordIds) {
        //Исключаем несуществующие записи
        String sql = String.format("select id from department where %s ", SqlUtils.transformToSqlInStatement("id", uniqueRecordIds));
        List<Long> recordIds = new LinkedList<Long>(uniqueRecordIds);
        List<Long> existRecords = new ArrayList<Long>();
        try {
            //Получаем список существующих записей среди входного набора
            existRecords = getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException ignored) {}

        for (Iterator<Long> it = recordIds.iterator(); it.hasNext();) {
            Long recordId = it.next();
            //Если запись не найдена среди существующих, то проставляем статус и удаляем ее из списка для остальных проверок
            if (existRecords.contains(recordId)) {
                it.remove();
            }
        }
        return recordIds;
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecords(@NotNull List<Long> uniqueRecordIds) {
        final List<ReferenceCheckResult> result = new ArrayList<ReferenceCheckResult>();
        Set<Long> recordIds = new HashSet<Long>(uniqueRecordIds);
        List<Long> existRecords = new ArrayList<Long>();

        //Исключаем несуществующие записи
        String sql = String.format("select id from department where %s and is_active != -1", SqlUtils.transformToSqlInStatement("id", recordIds));
        try {
            //Получаем список существующих записей среди входного набора
            existRecords = getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException ignored) {}
        for (Iterator<Long> it = recordIds.iterator(); it.hasNext();) {
            Long recordId = it.next();
            //Если запись не найдена среди существующих, то проставляем статус и удаляем ее из списка для остальных проверок
            if (!existRecords.contains(recordId)) {
                result.add(new ReferenceCheckResult(recordId, CheckResult.NOT_EXISTS));
                it.remove();
            }
        }
        return result;
    }
}
