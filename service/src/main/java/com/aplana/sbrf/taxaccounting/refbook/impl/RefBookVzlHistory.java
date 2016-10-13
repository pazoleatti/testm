package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookVzlHistoryDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Провайдер для справочника "История взаимозависимых лиц" в отдельной таблице REF_BOOK_VZL_HISTORY
 * @author dloshkarev
 */
@Service("refBookVzlHistory")
@Transactional
public class RefBookVzlHistory extends AbstractRefBookDataProvider {

    public static final Long REF_BOOK_ID = RefBookVzlHistoryDao.REF_BOOK_ID;
    public static final String TABLE_NAME = RefBookVzlHistoryDao.TABLE_NAME;

    @Autowired
    private RefBookVzlHistoryDao dao;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private LockDataService lockService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(@NotNull Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRecords(REF_BOOK_ID, TABLE_NAME, pagingParams, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        List<Date> list = new ArrayList<Date>();
        list.add(Calendar.getInstance().getTime());
        return list;
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return dao.getRecordData(recordId).get(attribute.getAlias());
    }

    @Override
    public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
        return refBookDao.dereferenceValues(TABLE_NAME, attributeId, recordIds);
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return refBookDao.getUniqueRecordIds(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookDao.getRecordsCount(REF_BOOK_ID, TABLE_NAME, filter);
    }

    @Override
    public Long getRowNum(Date version, Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(REF_BOOK_ID, TABLE_NAME, recordId, filter, sortAttribute, isSortAscending, null);
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDao.getInactiveRecordsInPeriod(TABLE_NAME, recordIds, periodFrom, periodFrom, true);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(@NotNull Long recordId) {
        return dao.getRecordData(recordId);
    }

    @Override
    public void insertRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        dao.createRecords(records);
    }

    @Override
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        dao.updateRecord(uniqueRecordId, records);
    }

    @Override
    public void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        try {
            refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, uniqueRecordIds, false);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                throw new ServiceLoggerException("Элемент справочника не удален, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Элемент справочника не удален, обнаружены фатальные ошибки!");
            }
        }
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        //Тут все так же как и в RefBookUniversal
        deleteRecordVersions(logger, uniqueRecordIds);
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        //Тут все так же как и в RefBookUniversal
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        //Устанавливаем блокировку на текущий справочник
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(REF_BOOK_ID, ReportType.EDIT_REF_BOOK);
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(REF_BOOK_ID);
        if (lockType == null && lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName())) == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = refBookFactory.generateTaskKey(attribute.getRefBookId(), ReportType.EDIT_REF_BOOK);
                        if (!lockedObjects.contains(referenceLockKey)) {
                            if (refBookFactory.getLockTaskType(attribute.getRefBookId()) == null &&
                                    lockService.lock(referenceLockKey, userId, String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName())) == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }
                //Выполняем обновление записей
                try {
                    updateRecordVersionWithoutLock(logger, uniqueRecordId, versionFrom, versionTo, records);
                } catch (Exception e) {
                    throw new ServiceLoggerException(e.getLocalizedMessage(),
                            logEntryService.save(logger.getEntries()));
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
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        //Тут все так же как и в RefBookUniversal
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(REF_BOOK_ID, ReportType.EDIT_REF_BOOK);
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(REF_BOOK_ID);
        if (lockType == null && lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName())) == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = refBookFactory.generateTaskKey(attribute.getRefBookId(), ReportType.EDIT_REF_BOOK);
                        if (!lockedObjects.contains(referenceLockKey)) {
                            if (refBookFactory.getLockTaskType(attribute.getRefBookId()) == null &&
                                    lockService.lock(referenceLockKey, userId, String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName())) == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }
                try {
                    deleteRecordVersionsWithoutLock(logger, uniqueRecordIds);
                } catch (Exception e) {
                    throw new ServiceLoggerException(e.getLocalizedMessage(),
                            logEntryService.save(logger.getEntries()));
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
    public void insertRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        //Тут все так же как и в RefBookUniversal
        List<String> lockedObjects = new ArrayList<String>();
        String lockKey = refBookFactory.generateTaskKey(REF_BOOK_ID, ReportType.EDIT_REF_BOOK);
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(REF_BOOK_ID);
        if (lockType == null && lockService.lock(lockKey, taUserInfo.getUser().getId(),
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), refBook.getName())) == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = refBookFactory.generateTaskKey(attribute.getRefBookId(), ReportType.EDIT_REF_BOOK);
                        if (!lockedObjects.contains(referenceLockKey)) {
                            if (refBookFactory.getLockTaskType(attribute.getRefBookId()) == null &&
                                    lockService.lock(referenceLockKey, taUserInfo.getUser().getId(), String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName())) == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceException(String.format(LOCK_MESSAGE, attributeRefBook.getName()));
                            }
                        }
                    }
                }
                insertRecordsWithoutLock(taUserInfo, version, records);
            } finally {
                for (String lock : lockedObjects) {
                    lockService.unlock(lock, taUserInfo.getUser().getId());
                }
            }
        } else {
            throw new ServiceException(String.format(LOCK_MESSAGE, refBook.getName()));
        }
    }

    /*********************   Неиспользуемые методы ********************/

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsVersionEndWithoutLock(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecordsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNextVersion(Date version, @NotNull String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getEndVersion(Long recordId, Date versionFrom) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> isRecordsExist(List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsById(Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        RefBookRecordVersion version = new RefBookRecordVersion();
        version.setRecordId(uniqueRecordId);
        version.setVersionStart(null);
        version.setVersionEnd(null);
        return version;
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRecordVersionsCount(Long uniqueRecordId) {
        return 1;
    }

    @Override
    public List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }
}
