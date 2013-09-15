package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.DepartmentFilterTreeListener;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter.Filter;
import com.aplana.sbrf.taxaccounting.dao.mapper.RefBookValueMapper;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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
        RefBook refBook = refBookDao.get(refBookId);
        StringBuffer sb = new StringBuffer();
        Filter.getFilterQuery(filter, new DepartmentFilterTreeListener(refBook, sb));
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append("id ").append(RefBook.RECORD_ID_ALIAS);
        for(RefBookAttribute attribute: refBook.getAttributes()){
            sql.append(", ");
            sql.append(attribute.getAlias());
        }
        sql.append(" FROM (SELECT ");
        if (isSupportOver() && sortAttribute != null){
            sql.append("row_number() over (order by '" + sortAttribute.getAlias() + "') as row_number_over");
        } else {
            sql.append("rownum row_number_over");
        }
        sql.append(", d.* FROM DEPARTMENT d");
        if (sb.length() > 0){
            sql.append(" WHERE\n ");
            sql.append(sb.toString());
            sql.append("\n");
        }
        sql.append(")");
        List<Map<String, RefBookValue>> records;
        if (pagingParams != null) {
            sql.append(" WHERE row_number_over BETWEEN :offset AND :count");
            Map<String, Integer> params = new HashMap<String, Integer>();
            params.put("count", pagingParams.getStartIndex() + pagingParams.getCount());
            params.put("offset", pagingParams.getStartIndex());
            records = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RefBookValueMapper(refBook));
        } else {
            records = getNamedParameterJdbcTemplate().query(sql.toString(), new HashMap<String, Object>(), new RefBookValueMapper(refBook));
        }
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<Map<String, RefBookValue>>(records);
        result.setTotalCount(getJdbcTemplate().queryForInt("SELECT count(*) FROM DEPARTMENT"));
        return result;
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        RefBook refBook = refBookDao.get(refBookId);
        StringBuilder sql = new StringBuilder("SELECT id ");
        sql.append(RefBook.RECORD_ID_ALIAS);
        for(RefBookAttribute attribute: refBook.getAttributes()){
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
