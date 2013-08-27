package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Income101FilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ekuvshinov
 */
@Repository
@Transactional
public class RefBookIncome101DaoImpl extends AbstractDao implements RefBookIncome101Dao {

    @Autowired
    private RefBookDao refBookDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Integer reportPeriodId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        RefBook refBook = refBookDao.get(refBookId);
        StringBuffer sb = new StringBuffer();
        Filter.getFilterQuery(filter, new Income101FilterTreeListener(refBook, sb));
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append("id ").append(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", ");
            sql.append(attribute.getAlias());
        }
        sql.append(" FROM (SELECT ");
        if (isSupportOver() && sortAttribute != null) {
            sql.append("row_number() over (order by '").append(sortAttribute.getAlias()).append("') as row_number_over");
        } else {
            sql.append("rownum row_number_over");
        }
        sql.append(", i.* FROM INCOME_101 i");
        if (sb.length() > 0) {
            sql.append(" WHERE\n ");
            sql.append(sb.toString());
            sql.append("\n");
            sql.append(" AND i.report_period_id = :report_period_id\n");
        } else {
            sql.append(" WHERE i.report_period_id = :report_period_id\n ");
        }
        sql.append(")");
        Map<String, Integer> params = new HashMap<String, Integer>();
        params.put("report_period_id", reportPeriodId);
        if (pagingParams != null) {
            sql.append(" row_number_over BETWEEN :offset AND :count");
            params.put("count", pagingParams.getStartIndex() + pagingParams.getCount());
            params.put("offset", pagingParams.getStartIndex());
        }
        List<Map<String, RefBookValue>> records = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RefBookValueMapper(refBook));
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>();
        result.setRecords(records);
        result.setTotalRecordCount(getJdbcTemplate().queryForInt("SELECT count(*) FROM INCOME_101 WHERE report_period_id = ?", reportPeriodId));
        return result;
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        RefBook refBook = refBookDao.get(refBookId);
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append("id ").append(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", ");
            sql.append(attribute.getAlias());
        }
        sql.append(" FROM INCOME_101 WHERE report_period_id = ?");

        return getJdbcTemplate().queryForObject(sql.toString(), new RefBookValueMapper(refBook), recordId);
    }

    @Override
    public RefBookValue getValue(Long refBookId, Long recordId, Long attributeId) {
        RefBook refBook = refBookDao.get(refBookId);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        Map<String, RefBookValue> result = getRecordData(refBookId, recordId);
        if (result.containsKey(attribute.getAlias())) {
            return result.get(attribute.getAlias());
        }
        return null;
    }

    @Override
    public void insert(Long refBookId, Integer reportPeriodId, List<Map<String, RefBookValue>> records) {
        if (records.size() == 0) {
            return;
        }
        for (Map<String, RefBookValue> record : records) {
            StringBuilder sql = new StringBuilder("INSERT INTO INCOME_101 ");
            StringBuilder value = new StringBuilder();
            StringBuilder params = new StringBuilder();
            Object[] values = new Object[record.size()];
            int[] types = new int[record.size()];
            int i = 0;
            for (Map.Entry<String, RefBookValue> v : record.entrySet()) {
                if (!v.getKey().equals(RefBook.RECORD_ID_ALIAS)) {
                    params.append(v.getKey());
                    params.append(", ");
                    value.append("?, ");
                    switch (v.getValue().getAttributeType()) {
                        case STRING:
                            values[i] = v.getValue().getStringValue();
                            types[i] = Types.VARCHAR;
                            break;
                        case NUMBER:
                            values[i] = v.getValue().getNumberValue();
                            types[i] = Types.NUMERIC;
                            break;
                        case DATE:
                            values[i] = v.getValue().getDateValue();
                            types[i] = Types.DATE;
                            break;
                        case REFERENCE:
                            // FIXME тут не планировались атрибуты ссылки и когда запланируют это надо реализовать
                            break;
                    }
                    i++;
                }
            }
            sql.append("(");
            sql.append(params.substring(0, params.length() - 2));
            sql.append(")");
            sql.append(" VALUES (");
            sql.append(value.substring(0, value.length() - 2));
            sql.append(")");
            getJdbcTemplate().update(sql.toString(), values, types);
        }
    }

    @Override
    public void deleteAll(Integer reportPeriodId) {
        String sql = "DELETE FROM income_101 WHERE report_period_id = ?";
        getJdbcTemplate().update(sql, reportPeriodId);
    }

    @Override
    public List<ReportPeriod> gerReportPeriods() {
        String sql = "SELECT DISTINCT report_period_id FROM income_101";
        return getJdbcTemplate().query(sql, new RowMapper<ReportPeriod>() {
            @Autowired
            private ReportPeriodDao reportPeriodDao;

            @Override
            public ReportPeriod mapRow(ResultSet rs, int rowNum) throws SQLException {
                return reportPeriodDao.get(rs.getInt(1));
            }
        });
    }

    @Override
    public void update(List<Map<String, RefBookValue>> records) {
        if (records.size() == 0) {
            return;
        }

        for (Map<String, RefBookValue> record : records) {
            StringBuilder sql = new StringBuilder("UPDATE income_101 SET ");
            StringBuilder set = new StringBuilder();
            Object[] values = new Object[record.size()];
            int[] types = new int[record.size()];
            int i = 0;
            for (Map.Entry<String, RefBookValue> attribute : record.entrySet()) {
                if (!attribute.getKey().equals(RefBook.RECORD_ID_ALIAS)) {
                    set.append(attribute.getKey());
                    set.append(" = ?, ");
                    switch (attribute.getValue().getAttributeType()) {
                        case STRING:
                            values[i] = attribute.getValue().getStringValue();
                            types[i] = Types.VARCHAR;
                            break;
                        case NUMBER:
                            values[i] = attribute.getValue().getNumberValue();
                            types[i] = Types.NUMERIC;
                            break;
                        case DATE:
                            values[i] = attribute.getValue().getDateValue();
                            types[i] = Types.DATE;
                            break;
                        case REFERENCE:
                            // FIXME тут не планировались атрибуты ссылки и когда запланируют это надо реализовать
                            break;
                    }
                    i++;
                }
            }
            sql.append(set.substring(0, set.length() - 2));
            sql.append(" WHERE id = ?");
            values[i] = record.get(RefBook.RECORD_ID_ALIAS).getNumberValue();
            types[i] = Types.NUMERIC;
            getJdbcTemplate().update(sql.toString(), values, types);
        }
    }

    @Override
    public void deleete(List<Long> ids) {
        if (ids.size() == 0) {
            return;
        }
        String sql = "DELETE FROM income_101 WHERE id = ?";
        JdbcTemplate jt = getJdbcTemplate();
        for (Long id : ids) {
            jt.update(sql, id);
        }
    }
}
