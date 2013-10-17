package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookUserDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Провайдер данных для таблицы user
 * @author auldanov
 */
@Service("RefBookUser")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RefBookUser implements RefBookDataProvider {

    public static final Long REF_BOOK_ID = 74L;

    @Autowired
    RefBookDao refBookDao;

    @Autowired
    private RefBookUserDao refBookUserDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookUserDao.getRecords(pagingParams, filter, sortAttribute);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookUserDao.getRecordData(recordId);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        List<Date> list = new ArrayList<Date>();
        list.add(Calendar.getInstance().getTime());
        return list;
    }

    @Override
    public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Date version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return refBookUserDao.getRecordData(recordId).get(attribute.getAlias());
    }
}
