package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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
public class RefBookDepartment implements RefBookDataProvider {
    public final static long REF_BOOK_ID = 30;
    @Autowired
    private RefBookDepartmentDao refBookDepartmentDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        // TODO сделать фильтр и sortAttribute
        return refBookDepartmentDao.getRecords(REF_BOOK_ID, pagingParams, filter, sortAttribute);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return null;  // нет нужды реализовывать смотри коментарии http://jira.aplana.com/browse/SBRFACCTAX-3245
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookDepartmentDao.getRecordData(REF_BOOK_ID, recordId);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        // В справочнике департментов нет версий там всегда актуальная информация, по крайне мере на текущий момент
        List<Date> result = new ArrayList<Date>(1);
        result.add(new Date());
        return result;
    }

    @Override
    public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        // нет нужды реализовывать смотри коментарии http://jira.aplana.com/browse/SBRFACCTAX-3245
    }

    @Override
    public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        // нет нужды реализовывать смотри коментарии http://jira.aplana.com/browse/SBRFACCTAX-3245
    }

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
        // нет нужды реализовывать смотри коментарии http://jira.aplana.com/browse/SBRFACCTAX-3245
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        return null;
    }
}
