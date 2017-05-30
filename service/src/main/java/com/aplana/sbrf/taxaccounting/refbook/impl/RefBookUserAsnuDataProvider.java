package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

@Service("refBookUserAsnu")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
/**
 * Провайдер данных необходим чтобы обновлять справочник "Ограничение доступа по АСНУ"
 */
public class RefBookUserAsnuDataProvider extends AbstractReadOnlyRefBook {

    @Autowired
    TAUserDao taUserDao;

    @Autowired
    RefBookSimpleReadOnly refBookSimpleReadOnly;

    @Autowired
    RefBookSimpleDataProvider refBookSimpleDataProvider;

    @Autowired
    protected RefBookFactory refBookFactory;

    @Autowired
    private LockDataService lockService;

    @Autowired
    private LogEntryService logEntryService;

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> record) {
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(getRefBookId(), ReportType.EDIT_REF_BOOK);
        if (isNotLocked(getRefBook(), userId, lockKey)) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                lockReferencedBooks(logger, lockedObjects);
                TAUser user = taUserDao.getUser(record.get("USER_ID").getReferenceValue().intValue());
                Long asnuId = record.get("ASNU_ID").getReferenceValue();
                if (!user.getAsnuIds().contains(asnuId)) {
                    user.getAsnuIds().add(asnuId);
                    taUserDao.updateUserAsnu(user);
                }
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, userId);
                }
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, refBook.getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(getRefBookId(), ReportType.EDIT_REF_BOOK);
        if (isNotLocked(getRefBook(), userId, lockKey)) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                lockReferencedBooks(logger, lockedObjects);
                Map<String, RefBookValue> record = getRecordData(uniqueRecordIds.get(0));
                TAUser user = taUserDao.getUser(record.get("USER_ID").getReferenceValue().intValue());
                Long asnuId = record.get("ASNU_ID").getReferenceValue();
                user.getAsnuIds().remove(asnuId);
                taUserDao.updateUserAsnu(user);
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, userId);
                }
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, refBook.getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter,
                                                              RefBookAttribute sortAttribute, boolean isSortAscending) {

        return refBookDao.getRecords(getRefBookId(), getTableName(), pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsVersion(Date versionFrom, Date versionTo, PagingParams pagingParams, String filter) {
        return refBookSimpleReadOnly.getRecordsVersion(versionFrom, versionTo, pagingParams, filter);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookSimpleReadOnly.getChildrenRecords(parentRecordId, version, pagingParams, filter, sortAttribute);
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(getRefBookId(), getTableName(), recordId, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookDao.getRecordData(getRefBookId(), getTableName(), recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        throw new UnsupportedOperationException();
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
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        return refBookSimpleReadOnly.getAttributesValues(attributePairs);
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return refBookSimpleReadOnly.getUniqueRecordIds(version, filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookDao.getRecordsCount(getRefBookId(), getTableName(), filter);
    }

    @Override
    public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
        return refBookSimpleReadOnly.dereferenceValues(attributeId, recordIds);
    }

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookSimpleReadOnly.getInactiveRecordsInPeriod(recordIds, periodFrom, periodTo);
    }

    public String getTableName() {
        return refBook.getTableName();
    }

    private boolean isNotLocked(RefBook refBook, int userId, String lockKey) {
        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(refBook.getId());
        return lockType == null && lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName())) == null;
    }

    private void lockReferencedBooks(@NotNull Logger logger, List<String> lockedObjects) {
        int userId = logger.getTaUserInfo().getUser().getId();
        List<RefBookAttribute> attributes = getRefBook().getAttributes();
        for (RefBookAttribute attribute : attributes) {
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                String referenceLockKey = refBookFactory.generateTaskKey(attribute.getRefBookId(), ReportType.EDIT_REF_BOOK);
                if (!lockedObjects.contains(referenceLockKey)) {
                    if (isNotLocked(attributeRefBook, userId, referenceLockKey)) {
                        lockedObjects.add(referenceLockKey);
                    } else {
                        throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                logEntryService.save(logger.getEntries()));
                    }
                }
            }
        }
    }

}
