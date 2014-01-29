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

import java.util.HashMap;
import java.util.Map;

/**
 * User: ekuvshinov
 */
@Repository
public class RefBookDepartmentDaoImpl extends AbstractDao implements RefBookDepartmentDao {

	private static final String TABLE_NAME = "DEPARTMENT";

	@Autowired
	private RefBookUtils refBookUtils;

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
}
