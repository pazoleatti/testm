package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * <p>
 * Абстрактный класс для провайдеров данных справочников. Предназначен для справочников без возможности редактирования,
 * и, следовательно, без версионирования и содержащий только одну версию. В качестве версии (даты начала актуальности)
 * каждый раз возвращается "01.01.1970".
 * </p>
 * <p>
 * Реализует базовые методы для чтения данных плоского справочника. Вся специфика определяется в классах наследниках.
 * В методах, которые относятся к API редактирования выбрасываются исключения {@link UnsupportedOperationException}.
 * </p>
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 29.01.14 17:11
 */
public abstract class AbstractReadOnlyRefBook implements RefBookDataProvider {

    protected RefBook refBook;

    @Autowired
    RefBookDao refBookDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter,
                                                              RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = refBookDao.get(getRefBookId());
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return getRecordData(recordId).get(attribute.getAlias());
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        RefBookRecordVersion version = new RefBookRecordVersion();
        version.setRecordId(uniqueRecordId);
        Date d = new Date(0);
        version.setVersionStart(d);
        version.setVersionEnd(d);
        return version;
    }

    @Override
    public List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersions(Logger logger, Date versionFrom, Date versionTo, Set<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
        return uniqueRecordId;
    }

    public Long getRefBookId() {
        if (refBook == null) {
            throw new IllegalArgumentException("Field \"refBook\" must be set");
        }
        return refBook.getId();
    }

    public RefBook getRefBook() {
        return refBook;
    }

    public void setRefBook(RefBook refBook) {
        this.refBook = refBook;
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecordsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }
}