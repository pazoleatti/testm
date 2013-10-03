package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.DepartmentFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ekuvshinov
 */
@Repository
@Transactional
public class RefBookDepartmentDaoImpl extends AbstractDao implements RefBookDepartmentDao {
    @Autowired
    private RefBookDao refBookDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        PreparedStatementData ps = new PreparedStatementData();
        RefBook refBook = refBookDao.get(refBookId);
        ps.appendQuery("SELECT ");
        ps.appendQuery("id ");
        ps.appendQuery(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            ps.appendQuery(", ");
            ps.appendQuery(attribute.getAlias());
        }
        ps.appendQuery(" FROM (SELECT ");
        if (isSupportOver() && sortAttribute != null) {
            ps.appendQuery("row_number() over (order by '" + sortAttribute.getAlias() + "') as row_number_over");
        } else {
            ps.appendQuery("rownum row_number_over");
        }
        ps.appendQuery(", d.* FROM DEPARTMENT d");

        PreparedStatementData filterPS = new PreparedStatementData();
        Filter.getFilterQuery(filter, new DepartmentFilterTreeListener(refBook, filterPS));
        if (filterPS.getQuery().length() > 0) {
            ps.appendQuery(" WHERE\n ");
            ps.appendQuery(filterPS.getQuery().toString());
            ps.appendQuery("\n");
            ps.addParam(filterPS.getParams());
        }
        ps.appendQuery(")");
        List<Map<String, RefBookValue>> records;
        if (pagingParams != null) {
            ps.appendQuery(" WHERE row_number_over BETWEEN ? AND ?");
            ps.addParam(pagingParams.getStartIndex());
            ps.addParam(pagingParams.getStartIndex() + pagingParams.getCount());
            records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        } else {
            records = getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RefBookValueMapper(refBook));
        }
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        if (isSupportOver()) {
            result.setTotalCount(getJdbcTemplate().queryForInt("SELECT count(*) FROM (" + ps.getQuery().toString() + ")", ps.getParams()));
        } else {
            // Бд тестовая тут магия
            result.setTotalCount(getJdbcTemplate().queryForInt("SELECT count(*) FROM DEPARTMENT"));
        }

        return result;
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        RefBook refBook = refBookDao.get(refBookId);
        StringBuilder sql = new StringBuilder("SELECT id ");
        sql.append(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", ");
            sql.append(attribute.getAlias());
        }
        sql.append(" FROM department WHERE id = :id");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("id", recordId);
        List<Map<String, RefBookValue>> records = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RefBookValueMapper(refBook));
        Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
        if (records.size() == 1) {
            result = records.get(0);
        }
        return result;
    }
}
