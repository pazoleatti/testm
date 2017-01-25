package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Универсальный провайдер данных для редактируемых версионированных справочников, хранящихся в отдельных таблицах.
 */
@Service("refBookSimpleDataProvider")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookSimpleDataProvider extends AbstractRefBookDataProvider {

    private static final String MSG_REFBOOK_NOT_EDITABLE_AND_VERSIONED = "Справочник %s (ID=%d) не поддерживает версионирование и редактирование";


    @Autowired
    private LockDataService lockService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private RefBookSimpleDao dao;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookSimpleReadOnly readOnlyProvider;
    @Autowired
    private RefBookSimpleDataProviderHelper helper;

    private RefBook refBook;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(@NotNull Date version, PagingParams pagingParams, String filter,
                                                              RefBookAttribute sortAttribute, boolean isSortAscending) {
        return dao.getRecords(getRefBook().getId(), version, pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version,
                                                                      PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
        /*if (!getRefBook().isHierarchic()) {
            throw new IllegalArgumentException(String.format(RefBookDaoImpl.NOT_HIERARCHICAL_REF_BOOK_ERROR, getRefBook().getName(), getRefBook().getId()));
        }
        return dao.getChildrenRecords(getRefBook().getTableName(), getRefBook().getId(), version, parentRecordId, pagingParams, filter, sortAttribute);*/
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
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return dao.getUniqueRecordIds(getRefBook().getId(), getRefBook().getTableName(), version, filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return dao.getRecordsCount(getRefBook().getId(), getRefBook().getTableName(), version, filter);
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
    public Long getRowNum(Date version, Long recordId, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return dao.getRowNum(getRefBook().getId(), version, recordId, filter, sortAttribute, isSortAscending);
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(@NotNull Long recordId) {
        return refBookDao.getRecordData(getRefBook().getId(), getRefBook().getTableName(), recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        if (getRefBook().isVersioned()) {
            return dao.getVersions(getRefBook().getTableName(), startDate, endDate);
        } else {
            return readOnlyProvider.getVersions(startDate, endDate);
        }
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsById(Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return dao.getRecordVersionsByRecordId(getRefBook().getId(), recordId, pagingParams, filter, sortAttribute);
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        return dao.getRecordVersionInfo(getRefBook().getTableName(), uniqueRecordId);
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRecordVersionsCount(Long uniqueRecordId) {
        return dao.getRecordVersionsCount(getRefBook().getTableName(), uniqueRecordId);
    }

    @Override
    public List<Long> createRecordVersion(@NotNull Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
        /*if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        //Устанавливаем блокировку на тевущий справочник
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(getRefBook().getId(), ReportType.EDIT_REF_BOOK);
        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(getRefBook().getId());
        if (lockType == null && lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), getRefBook().getName())) == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = getRefBook().getAttributes();
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
                //Выполняем создание записей
                try {
                    return createRecordVersionWithoutLock(logger, versionFrom, versionTo, records);
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
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, getRefBook().getName()),
                    logEntryService.save(logger.getEntries()));
        }*/
    }

    @Override
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
        /*
        try {
            List<Long> excludedVersionEndRecords = new ArrayList<Long>();
            //Признак того, что для проверок дата окончания была изменена (была использована дата начала следующей версии)
            boolean dateToChangedForChecks = false;

            if (!refBook.isVersioned()) {
                //Устанавливаем минимальную дату
                versionFrom = new Date(0L);
            }

            long countIds = 0;
            for (RefBookRecord record : records) {
                if (record.getRecordId() == null) {
                    countIds++;
                }
                record.setVersionTo(versionTo);
            }

            //Проверка корректности
            helper.checkCorrectness(logger, refBook, null, versionFrom, records);

            if (!getRefBookId().equals(RefBook.DEPARTMENT_CONFIG_TRANSPORT) &&
                    !getRefBookId().equals(RefBook.DEPARTMENT_CONFIG_INCOME) &&
                    !getRefBookId().equals(RefBook.DEPARTMENT_CONFIG_DEAL) &&
                    !getRefBookId().equals(RefBook.DEPARTMENT_CONFIG_VAT) &&
                    !getRefBookId().equals(RefBook.DEPARTMENT_CONFIG_PROPERTY) &&
                    !getRefBookId().equals(RefBook.DEPARTMENT_CONFIG_LAND) &&
                    !getRefBookId().equals(RefBook.WithTable.PROPERTY.getTableRefBookId()) &&
                    !getRefBookId().equals(RefBook.WithTable.TRANSPORT.getTableRefBookId()) &&
                    !getRefBookId().equals(RefBook.WithTable.INCOME.getTableRefBookId()) &&
                    !getRefBookId().equals(RefBook.WithTable.LAND.getTableRefBookId())
                    ) {

                if (refBook.isVersioned()) {
                    for (RefBookRecord record : records) {
                        //Проверка пересечения версий
                        if (record.getRecordId() != null) {
                            boolean needToCreateFakeVersion = helper.crossVersionsProcessing(refBookDao.checkCrossVersions(getRefBookId(), record.getRecordId(), versionFrom, record.getVersionTo(), null),
                                    refBook, versionFrom, record.getVersionTo(), logger);
                            if (!needToCreateFakeVersion) {
                                //Добавляем запись в список тех, для которых не будут созданы фиктивные версии
                                excludedVersionEndRecords.add(record.getRecordId());
                            }
                        }
                    }
                }
            }

            //Создание настоящей и фиктивной версии
            for (RefBookRecord record : records) {
                if (dateToChangedForChecks) {
                    //Возвращаем обратно пустую дату начала, т.к была установлена дата начала следующей версии для проверок
                    record.setVersionTo(null);
                    versionTo = null;
                }
            }
            return helper.createVersions(refBook, versionFrom, versionTo, records, countIds, excludedVersionEndRecords, logger);
        } catch (DataAccessException e) {
            throw new ServiceException("Запись не сохранена. Обнаружены фатальные ошибки!", e);
        }*/
    }

    @Override
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

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
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRecordId(@NotNull Long uniqueRecordId) {
        return dao.getRecordId(getRefBook().getTableName(), uniqueRecordId);
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDao.getInactiveRecords(getRefBook().getTableName(), recordIds);
    }

    @Override
    public void insertRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
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
    public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
        return refBookDao.dereferenceValues(getRefBook().getTableName(), attributeId, recordIds);
    }

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }

    public RefBook getRefBook() {
        if (refBook == null) {
            throw new IllegalArgumentException("refBook must be set");
        }
        return refBook;
    }

    public void setRefBook(RefBook refBook) {
        if (!isRefBookSupported(refBook)) {
            throw new IllegalArgumentException(String.format(MSG_REFBOOK_NOT_EDITABLE_AND_VERSIONED, refBook.getName(), refBook.getId()));
        }
        this.refBook = refBook;
    }

    public Long getRefBookId() {
        return getRefBook().getId();
    }

    public void setRefBookId(long refBookId) {
        RefBook probableRefBook = refBookDao.get(refBookId);
        if (!isRefBookSupported(probableRefBook)) {
            throw new IllegalArgumentException(String.format(MSG_REFBOOK_NOT_EDITABLE_AND_VERSIONED, probableRefBook.getName(), probableRefBook.getId()));
        }
        this.refBook = probableRefBook;
    }

    public void setRefBookId(RefBook.Id id) {
		setRefBookId(id.getId());
	}

	public void setRefBookId(RefBook refBook) {
		this.refBook = refBook;
	}

    public boolean isRefBookSupported(RefBook refBook) {
        return refBook.isVersioned() && !refBook.isReadOnly();
    }

    public boolean isRefBookSupported(long refBookId) {
        RefBook refBook = refBookDao.get(refBookId);
        return isRefBookSupported(refBook);
    }
}
