package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Универсальный провайдер данных для редактируемых версионированных справочников, хранящихся в отдельных таблицах.
 */
@Service("refBookSimpleDataProvider")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookSimpleDataProvider extends AbstractRefBookDataProvider {

    private static final String MSG_REFBOOK_NOT_VERSIONED = "Справочник %s (ID=%d) не поддерживает версионирование";
    public static final String CURRENT_USER_NOT_SET = "Текущий пользователь не установлен!";


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
    public Map<String, RefBookValue> getRecordData(@NotNull Long id) {
        return dao.getRecordData(getRefBook(), id);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        return dao.getRecordData(getRefBook(), recordIds);
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
        return dao.getRecordVersions(getRefBook(), uniqueRecordId, pagingParams, filter, sortAttribute, true);
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

    private void throwUnsupportedOperation(){
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> createRecordVersion(@NotNull Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {

        if (logger.getTaUserInfo() == null) {
            throw new ServiceException(CURRENT_USER_NOT_SET);
        }
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(getRefBook().getId(), ReportType.EDIT_REF_BOOK);
        if (isNotLocked(userId, lockKey)) {
            try {
                lockedObjects.add(lockKey);
                lockReferencedBooks(logger, lockedObjects);
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
        }
    }

    @Override
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {

        try {
            List<Long> excludedVersionEndRecords = new ArrayList<Long>();
            //Признак того, что для проверок дата окончания была изменена (была использована дата начала следующей версии)
            boolean dateToChangedForChecks = false;

            if (!getRefBook().isVersioned()) {
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
            helper.checkCorrectness(logger, getRefBook(), null, versionFrom, records);

            if (refBook.isVersioned()) {
                for (RefBookRecord record : records) {
                    //Проверка пересечения версий
                    if (record.getRecordId() != null) {
                        boolean needToCreateFakeVersion = helper.crossVersionsProcessing(dao.checkCrossVersions(getRefBook(), record.getRecordId(), versionFrom, record.getVersionTo(), null),
                                getRefBook(), versionFrom, record.getVersionTo(), logger);
                        if (!needToCreateFakeVersion) {
                            //Добавляем запись в список тех, для которых не будут созданы фиктивные версии
                            excludedVersionEndRecords.add(record.getRecordId());
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
        }
    }

    @Override
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersion(@NotNull Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throwUnsupportedOperation();

        if (logger.getTaUserInfo() == null) {
            throw new ServiceException(CURRENT_USER_NOT_SET);
        }
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(getRefBook().getId(), ReportType.EDIT_REF_BOOK);
        if (isNotLocked(userId, lockKey)) {
            try {
                lockedObjects.add(lockKey);
                lockReferencedBooks(logger, lockedObjects);
                try {
                    updateRecordVersionWithoutLock(logger, uniqueRecordId, versionFrom, versionTo, records);
                } catch (Exception e) {
                    throw new ServiceLoggerException(e.getLocalizedMessage(),
                            logEntryService.save(logger.getEntries()));
                }
            } finally {
                unlockObjects(lockedObjects, userId);
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, getRefBook().getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    private void unlockObjects(List<String> lockedObjects, int userId) {
        for (String lock : lockedObjects) {
            lockService.unlock(lock, userId);
        }
    }

    private boolean isNotLocked(int userId, String lockKey) {
        Pair<ReportType, LockData> lockType = refBookFactory.getLockTaskType(getRefBook().getId());
        return lockType == null && lockService.lock(lockKey, userId,
                String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), getRefBook().getName())) == null;
    }

    private void lockReferencedBooks(@NotNull Logger logger, List<String> lockedObjects) {
        int userId = logger.getTaUserInfo().getUser().getId();
        List<RefBookAttribute> attributes = getRefBook().getAttributes();
        for (RefBookAttribute attribute : attributes) {
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                String referenceLockKey = refBookFactory.generateTaskKey(attribute.getRefBookId(), ReportType.EDIT_REF_BOOK);
                if (!lockedObjects.contains(referenceLockKey)) {
                    if (refBookFactory.getLockTaskType(attribute.getRefBookId()) == null &&
                            lockService.lock(referenceLockKey, userId, String.format(LockData.DescriptionTemplate.REF_BOOK.getText(), attributeRefBook.getName())) == null) {
                        lockedObjects.add(referenceLockKey);
                    } else {
                        throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                logEntryService.save(logger.getEntries()));
                    }
                }
            }
        }
    }

    @Override
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throwUnsupportedOperation();
        /*
        try {
            List<RefBookAttribute> attributes = getRefBook().getAttributes();
            boolean isJustNeedValuesUpdate = (versionFrom == null && versionTo == null);

            //Получаем идентификатор записи справочника без учета версий
            Long recordId = refBookDao.getRecordId(uniqueRecordId);
            //Получаем еще неотредактированную версию
            RefBookRecordVersion oldVersionPeriod = refBookDao.getRecordVersionInfo(uniqueRecordId);

            RefBookRecord refBookRecord = new RefBookRecord();
            refBookRecord.setUniqueRecordId(uniqueRecordId);
            refBookRecord.setRecordId(refBookDao.getRecordId(uniqueRecordId));
            refBookRecord.setValues(records);
            refBookRecord.setVersionTo(versionTo);

            //Проверка корректности
            helper.checkCorrectness(logger, getRefBook(), uniqueRecordId, versionFrom, Arrays.asList(refBookRecord));

            if (getRefBook().isHierarchic()) {
                RefBookValue oldParent = refBookDao.getValue(uniqueRecordId, getRefBook().getAttribute(RefBook.RECORD_PARENT_ID_ALIAS).getId());
                RefBookValue newParent = records.get(RefBook.RECORD_PARENT_ID_ALIAS);
                //Проверка зацикливания
                if (!newParent.equals(oldParent) &&
                        refBookDao.hasLoops(uniqueRecordId, newParent.getReferenceValue())) {
                    //Цикл найден, формируем сообщение
                    String parentRecordName = refBookDao.buildUniqueRecordName(getRefBook(),
                            refBookDao.getUniqueAttributeValues(getRefBookId(), newParent.getReferenceValue()));
                    String recordName = refBookDao.buildUniqueRecordName(getRefBook(),
                            refBookDao.getUniqueAttributeValues(getRefBookId(), uniqueRecordId));
                    if (getRefBook().isVersioned()) {
                        throw new ServiceException("Версия " + parentRecordName + " не может быть указана как родительская, т.к. входит в структуру дочерних элементов версии " + recordName);
                    } else {
                        throw new ServiceException("Запись " + parentRecordName + " не может быть указана как родительская, т.к. входит в структуру дочерних элементов записей " + recordName);
                    }
                }
            }

            boolean isRelevancePeriodChanged = false;
            RefBookRecordVersion previousVersion = null;
            if (!isJustNeedValuesUpdate) {
                assert versionFrom != null;
                isRelevancePeriodChanged = !versionFrom.equals(oldVersionPeriod.getVersionStart())
                        || (versionTo != null && !versionTo.equals(oldVersionPeriod.getVersionEnd()))
                        || (oldVersionPeriod.getVersionEnd() != null && !oldVersionPeriod.getVersionEnd().equals(versionTo));

                if (isRelevancePeriodChanged) {
                    //Проверка пересечения версий
                    //Проверяем следующую версию после даты окочания
                    RefBookRecordVersion oldNextVersion = refBookDao.getNextVersion(getRefBookId(), recordId, oldVersionPeriod.getVersionStart());
                    if (versionTo != null && oldNextVersion != null && (versionTo.equals(oldNextVersion.getVersionStart()) || versionTo.after(oldNextVersion.getVersionStart())) ||
                            versionTo == null && oldNextVersion != null) {
                        //TODO: поведение отличается от поведения при создании записи, там исключения нет, просто изменяется дата окончания на дату начала след. версии
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                    //Проверяем предыдущую версию до даты начала
                    previousVersion = refBookDao.getPreviousVersion(getRefBookId(), recordId, oldVersionPeriod.getVersionStart());
                    if (previousVersion != null &&
                            (versionFrom.equals(previousVersion.getVersionEnd())
                                    || versionFrom.before(previousVersion.getVersionEnd())
                                    || versionFrom.before(previousVersion.getVersionStart()))) {
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                }
            }

            boolean isValuesChanged = checkValuesChanged(uniqueRecordId, records);

            if (isValuesChanged) {
                //Если значения атрибутов изменились, то проверяем все использования записи, без учета периода
                checkUsages(getRefBook(), Arrays.asList(uniqueRecordId), versionFrom, versionTo, null, logger, "Изменение невозможно, обнаружено использование элемента справочника!");
            }

            //Обновление периода актуальности
            if (isRelevancePeriodChanged) {
                if (!isValuesChanged) {
                    //Если изменился только период актуальности, то ищем все ссылки не пересекающиеся с новым периодом, но которые действовали в старом
                    checkUsages(getRefBook(), Arrays.asList(uniqueRecordId), versionFrom, versionTo, false, logger, "Изменение невозможно, обнаружено использование элемента справочника!");
                }
                if (!refBook.isVersioned()) {
                    //Если справочник не версионный, то нет смысла проверять пересечения
                    //checkUsages(refBook, Arrays.asList(uniqueRecordId), versionFrom, versionTo, null, logger, "Изменение невозможно, обнаружено использование элемента справочника!");
                } else {
                    List<Long> uniqueIdAsList = Arrays.asList(uniqueRecordId);
                    if (previousVersion != null && (previousVersion.isVersionEndFake() && SimpleDateUtils.addDayToDate(previousVersion.getVersionEnd(), 1).equals(versionFrom))) {
                        //Если установлена дата окончания, которая совпадает с существующей фиктивной версией - то она удаляется
                        Long previousVersionEnd = refBookDao.findRecord(getRefBookId(), recordId, versionFrom);
                        refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, Arrays.asList(previousVersionEnd), false);
                    }

                    boolean delayedUpdate = false;
                    if (oldVersionPeriod.getVersionEnd() != null && versionFrom.equals(oldVersionPeriod.getVersionEnd())) {
                        //Обновляем дату начала актуальности, если не совпадает с датой окончания
                        refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, uniqueRecordId, versionFrom);
                    } else {
                        delayedUpdate = true;
                    }

                    //Получаем запись - окончание версии. Если = null, то версия не имеет конца
                    List<Long> relatedVersions = refBookDao.getRelatedVersions(uniqueIdAsList);
                    if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                        throw new ServiceException("Обнаружено несколько фиктивных версий");
                    }
                    if (versionTo != null) {
                        //Существует другая версия, дата начала которой = нашей новой дате окончания?
                        boolean isVersionEndAlreadyExists = refBookDao.isVersionsExist(getRefBookId(), Arrays.asList(recordId), SimpleDateUtils.addDayToDate(versionTo, 1));
                        if (relatedVersions.isEmpty() && !isVersionEndAlreadyExists) {
                            //Создаем новую фиктивную версию - дату окончания
                            refBookDao.createFakeRecordVersion(getRefBookId(), recordId, SimpleDateUtils.addDayToDate(versionTo, 1));
                        }

                        if (!relatedVersions.isEmpty() && !oldVersionPeriod.getVersionEnd().equals(versionTo)) {
                            if (!isVersionEndAlreadyExists) {
                                //Изменяем существующую дату окончания
                                refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, relatedVersions.get(0), SimpleDateUtils.addDayToDate(versionTo, 1));
                            } else {
                                //Удаляем дату окончания. Теперь дата окончания задается началом следующей версии
                                refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, relatedVersions, false);
                            }
                        }
                    }

                    if (!relatedVersions.isEmpty() && versionTo == null) {
                        //Удаляем фиктивную запись - теперь у версии нет конца
                        refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, relatedVersions, false);
                    }

                    if (delayedUpdate) {
                        //Обновляем дату начала актуальности, если ранее это было отложено т.к она совпадала с датой окончания (теперь она изменена)
                        refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, uniqueRecordId, versionFrom);
                    }
                }
            }

            //Обновление значений атрибутов версии
            refBookDao.updateRecordVersion(getRefBookId(), uniqueRecordId, records);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                throw new ServiceLoggerException("Запись не сохранена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Запись не сохранена, обнаружены фатальные ошибки!");
            }
        }
    */
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
            throw new IllegalArgumentException(String.format(MSG_REFBOOK_NOT_VERSIONED, refBook.getName(), refBook.getId()));
        }
        this.refBook = refBook;
    }

    public Long getRefBookId() {
        return getRefBook().getId();
    }

    public void setRefBookId(long refBookId) {
        RefBook probableRefBook = refBookDao.get(refBookId);
        if (!isRefBookSupported(probableRefBook)) {
            throw new IllegalArgumentException(String.format(MSG_REFBOOK_NOT_VERSIONED, probableRefBook.getName(), probableRefBook.getId()));
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
        return refBook.isVersioned();
    }

    public boolean isRefBookSupported(long refBookId) {
        RefBook refBook = refBookDao.get(refBookId);
        return isRefBookSupported(refBook);
    }
}
