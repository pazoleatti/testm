package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.refbook.ReferenceCheckResult;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Универсальный провайдер данных для справочников, хранящихся в отдельных таблицах. Только для чтения и без версионирования.
 */
@Service("refBookSimpleReadOnly")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookSimpleReadOnly extends AbstractReadOnlyRefBook {

    private RefBookSimpleDao refBookSimpleDao;
    private CommonRefBookService commonRefBookService;

    public RefBookSimpleReadOnly(RefBookSimpleDao refBookSimpleDao, CommonRefBookService commonRefBookService) {
        this.refBookSimpleDao = refBookSimpleDao;
        this.commonRefBookService = commonRefBookService;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsWithVersionInfo(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, String direction) {
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecordsWithVersionInfo(getRefBook(), version, pagingParams, filter, sortAttribute, direction);
        return commonRefBookService.dereference(getRefBook(), records);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter,
                                                              RefBookAttribute sortAttribute, boolean isSortAscending) {
        String whereClause = "ID <> -1";
        return refBookDao.getRecords(getRefBookId(), refBook.getTableName(), pagingParams, filter, sortAttribute, isSortAscending, whereClause);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsVersion(Date versionFrom, Date versionTo, PagingParams pagingParams, String filter) {
        return getRecords(versionTo, pagingParams, filter, null);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookDao.getRecordData(getRefBookId(), refBook.getTableName(), recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        return refBookSimpleDao.getRecordData(refBookDao.get(getRefBookId()), recordIds);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordDataWhere(String where) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordDataVersionWhere(String where, Date version) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return refBookDao.getUniqueRecordIds(getRefBookId(), refBook.getTableName(), filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookSimpleDao.getRecordsCount(refBookDao.get(getRefBookId()), version, filter);
    }

    @Override
    public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
        return refBookDao.dereferenceValues(refBook.getTableName(), attributeId, recordIds);
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDao.getInactiveRecords(refBook.getTableName(), recordIds);
    }
}