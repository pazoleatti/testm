package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
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
import java.util.Map;

/**
 * User: ekuvshinov
 */
@Repository
public class RefBookDepartmentDaoImpl extends AbstractDao implements RefBookDepartmentDao {

	private static final String TABLE_NAME = "DEPARTMENT";

    @Autowired
    private RefBookDao refBookDao;

	@Autowired
	private RefBookUtils refBookUtils;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return refBookUtils.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        StringBuilder sql = new StringBuilder("SELECT id ");
        sql.append(RefBook.RECORD_ID_ALIAS);
        for (RefBookAttribute attribute : refBook.getAttributes()) {
            sql.append(", ");
            sql.append(attribute.getAlias());
        }
        sql.append(" FROM department WHERE id = :id");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("id", recordId);
        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RefBookValueMapper(refBook));
    }
}
