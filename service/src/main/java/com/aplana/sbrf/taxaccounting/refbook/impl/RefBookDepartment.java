package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Провайдер для работы со справочником подразделений
 * В текущей версии расчитано что он будет использоваться только  для получение данных, возможности вставки данных НЕТ, версионирования НЕТ (данные в одном экземпляре)
 * Смотри http://jira.aplana.com/browse/SBRFACCTAX-3245
 * User: ekuvshinov
 */
@Service("refBookDepartment")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookDepartment implements RefBookDataProvider {

    public static final Long REF_BOOK_ID = RefBookDepartmentDao.REF_BOOK_ID;

    @Autowired
    RefBookDao refBookDao;

    @Autowired
    private RefBookDepartmentDao refBookDepartmentDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDepartmentDao.getRecords(pagingParams, filter, sortAttribute);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookDepartmentDao.getRecordData(recordId);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        // В справочнике департментов нет версий там всегда актуальная информация, по крайне мере на текущий момент
        List<Date> result = new ArrayList<Date>(1);
        result.add(new Date(0));
        return result;
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
        return refBookDepartmentDao.getRecordData(recordId).get(attribute.getAlias());
    }
}
