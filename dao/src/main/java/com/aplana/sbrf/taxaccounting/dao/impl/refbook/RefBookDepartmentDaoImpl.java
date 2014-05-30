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
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookUtils.getRecordData(REF_BOOK_ID, TABLE_NAME, recordId);
    }

    private final static String CHECK_UNIQUE_MATCHES_FOR_NON_VERSION = "select id record_id from %s t where ";
    @Override
    public List<Pair<Long, String>> getMatchedRecordsByUniqueAttributes(Long refBookId, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        boolean hasUniqueAttributes = false;
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(CHECK_UNIQUE_MATCHES_FOR_NON_VERSION);
        for (RefBookAttribute attribute : attributes) {
            if (attribute.isUnique()) {
                hasUniqueAttributes = true;
                for (int i=0; i < records.size(); i++) {
                    Map<String, RefBookValue> values = records.get(i).getValues();
                    ps.appendQuery(String.format("t.%s = ?",  attribute.getAlias()));

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

                    if (i < records.size() - 1) {
                        ps.appendQuery(" or ");
                    }
                }
            }
        }


        try {
            if (hasUniqueAttributes) {
                return getJdbcTemplate().query(String.format(ps.getQuery().toString(), TABLE_NAME), ps.getParams().toArray(), new RowMapper<Pair<Long, String>>() {
                    @Override
                    public Pair<Long, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<Long, String>(SqlUtils.getLong(rs, "ID"), rs.getString("NAME"));
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
        PreparedStatementData ps = new PreparedStatementData();
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
        String ph = SqlUtils.preparePlaceHolders(records.size());
        try {
            return getJdbcTemplate().update(
                    String.format(CREATE_DEPARTMENT, ps.getQuery().toString().substring(0, ps.getQuery().toString().length() - 1), ph),
                    ps.getParams().toArray());
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void remove(long uniqueId) {
        try {
            getJdbcTemplate().update("delete from department where id = ?", uniqueId);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }
}
