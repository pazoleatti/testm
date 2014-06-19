package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

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

	private static final String TABLE_NAME = "DEPARTMENT";

    @Autowired
	private RefBookUtils refBookUtils;
    @Autowired
    ReportPeriodDao reportPeriodDao;
    @Autowired
    RefBookDao refBookDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
		return refBookUtils.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public Long getRowNum(Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookUtils.getRowNum(REF_BOOK_ID, TABLE_NAME, recordId, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookUtils.getRecordData(REF_BOOK_ID, TABLE_NAME, recordId);
    }

    private final static String CHECK_UNIQUE_MATCHES_FOR_NON_VERSION = "select id record_id, name from %s t where %s %s (%s)";
    @Override
    public List<Pair<Long, String>> getMatchedRecordsByUniqueAttributes(Long recordId, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        boolean hasUniqueAttributes = false;
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(CHECK_UNIQUE_MATCHES_FOR_NON_VERSION);
        ArrayList<RefBookAttribute> uniqueAttrs = new ArrayList<RefBookAttribute>();
        for (RefBookAttribute attribute : attributes) {
            if (attribute.isUnique())
                uniqueAttrs.add(attribute);
        }
        String idOddsTag = "";
        if (recordId != null){
            ps.addParam(recordId);
            idOddsTag = "t.id <> ?";
        }

        String andTag = !uniqueAttrs.isEmpty() && recordId != null ? "AND" : "";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < uniqueAttrs.size(); i++) {
            RefBookAttribute attribute = uniqueAttrs.get(i);
            if (attribute.isUnique()) {
                hasUniqueAttributes = true;
                for (RefBookRecord record : records) {
                    Map<String, RefBookValue> values = record.getValues();
                    sb.append(String.format("t.%s = ?", attribute.getAlias()));

                    if (attribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                        ps.addParam(values.get(attribute.getAlias()).getStringValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        ps.addParam(values.get(attribute.getAlias()).getReferenceValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                        ps.addParam(values.get(attribute.getAlias()).getNumberValue());
                    }
                    if (attribute.getAttributeType().equals(RefBookAttributeType.DATE)) {
                        ps.addParam(values.get(attribute.getAlias()).getDateValue());
                    }

                    if (i < uniqueAttrs.size() - 1) {
                        sb.append(" or ");
                    }
                }
            }
        }


        try {
            if (hasUniqueAttributes) {
                return getJdbcTemplate().query(
                        String.format(ps.getQuery().toString(), TABLE_NAME, idOddsTag, andTag, sb.toString()), ps.getParams().toArray(),
                        new RowMapper<Pair<Long, String>>() {
                            @Override
                            public Pair<Long, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                                return new Pair<Long, String>(SqlUtils.getLong(rs, "record_id"), rs.getString("NAME"));
                            }
                        });
            } else {
                return new PagingResult<Pair<Long, String>>(new ArrayList<Pair<Long, String>>(0));
            }
        } catch (DataAccessException e){
            throw new DaoException("Ошибка при поиске уникальных значений в справочнике " + TABLE_NAME, e);
        }
    }

    @Override
    public List<Long> getPeriodsByTaxTypesAndDepartments(List<TaxType> taxTypes, List<Integer> departmentList) {
        return reportPeriodDao.getPeriodsByTaxTypesAndDepartments(taxTypes, departmentList);
    }

    private static String UPDATE_DEPARTMENT = "update department t set %s where id = ?";

    @Override
    public void update(long uniqueId, Map<String, RefBookValue> records,  List<RefBookAttribute> attributes) {
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
    public int create(Map<String, RefBookValue> records, List<RefBookAttribute> attributes) {
        final PreparedStatementData ps = new PreparedStatementData();
        for (RefBookAttribute attribute : attributes) {
            ps.appendQuery(attribute.getAlias() + ",");

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
        final String ph = SqlUtils.preparePlaceHolders(records.size());
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
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void remove(long uniqueId) {
        try {
            getJdbcTemplate().update("delete from department where id = ?", uniqueId);
        } catch (DataIntegrityViolationException e){
            throw new DaoException("Нарушение ограничения целостности. Возможно обнаружена порожденная запись.", e);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public int getRecordsCount(String filter) {
        return refBookUtils.getRecordsCount(REF_BOOK_ID, TABLE_NAME, filter);
    }

    private final static String GET_ATTRIBUTES_VALUES = "select \n" +
            "  attribute_id,\n" +
            "  record_id, \n" +
            "  value,\n" +
            "  data_type\n" +
            "from (\n" +
            "  with t as (\n" +
            "  select id as record_id, name, to_char(parent_id) as parent_id, to_char(type)as type, shortname, to_char(tb_index) as tb_index, sbrf_code, to_char(region_id) as region_id, to_char(is_active) as is_active, to_char(code) as code from department \n" +
            "  )\n" +
            "  select a.id as attribute_id, a.type as data_type, record_id, value from t\n" +
            "  unpivot \n" +
            "  (value for attribute_alias in (NAME, parent_id, type, shortname, tb_index, sbrf_code, region_id, is_active, code)) \n" +
            "  join ref_book_attribute a on attribute_alias = a.alias\n" +
            "  where ref_book_id = 30\n" +
            ") where (record_id, attribute_id) in ";

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        final Map<RefBookAttributePair, String> result = new HashMap<RefBookAttributePair, String>();
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(GET_ATTRIBUTES_VALUES);
        ps.appendQuery("(");
        for (Iterator<RefBookAttributePair> it = attributePairs.iterator(); it.hasNext();) {
            RefBookAttributePair pair = it.next();
            ps.appendQuery("(?,?)");
            ps.addParam(pair.getUniqueRecordId());
            ps.addParam(pair.getAttributeId());
            if (it.hasNext()) {
                ps.appendQuery(",");
            }
        }
        ps.appendQuery(")");
        getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(),
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        result.put(new RefBookAttributePair(rs.getLong("attribute_id"), rs.getLong("record_id")), rs.getString("value"));
                    }
                });
        return result;
    }
}
