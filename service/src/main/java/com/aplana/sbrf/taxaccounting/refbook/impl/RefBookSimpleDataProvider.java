package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookSimpleDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

import static java.util.Collections.singletonList;

/**
 * Универсальный провайдер данных для редактируемых версионированных справочников, хранящихся в отдельных таблицах.
 */
@Service("refBookSimpleDataProvider")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookSimpleDataProvider implements RefBookDataProvider {

    public static final String CURRENT_USER_NOT_SET = "Текущий пользователь не установлен!";
    private static final String CROSS_ERROR_MSG = "Обнаружено пересечение указанного срока актуальности с существующей версией!";
    private static final String MSG_REFBOOK_NOT_SUPPORTED = "Справочник %s (id=%d) не поддерживается провайдером";

    @Autowired
    private LockDataService lockService;
    @Autowired
    private CommonRefBookService commonRefBookService;
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
    public PagingResult<Map<String, RefBookValue>> getRecordsWithVersionInfo(Date version, PagingParams pagingParams,
                                                                             String filter, RefBookAttribute sortAttribute, String direction) {
        PagingResult<Map<String, RefBookValue>> records = refBookDao.getRecordsWithVersionInfo(getRefBook(), version, pagingParams, filter, sortAttribute, direction);
        return commonRefBookService.dereference(getRefBook(), records);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter,
                                                              RefBookAttribute sortAttribute, boolean isSortAscending) {
        return dao.getRecords(getRefBook(), version, pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordsVersion(Date versionFrom, Date versionTo, PagingParams pagingParams, String filter) {
        return dao.getVersionsInPeriod(getRefBook(), versionFrom, versionTo, filter);
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return dao.getUniqueRecordIds(getRefBook(), version, filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return dao.getRecordsCount(getRefBook(), version, filter);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(@NotNull Long id) {
        return dao.getRecordData(getRefBook(), id);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> uniqueRecordIds) {
        return dao.getRecordData(getRefBook(), uniqueRecordIds);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordDataWhere(String where) {
        return dao.getRecordDataWhere(getRefBook(), where);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordDataVersionWhere(String where, Date version) {
        RefBook rf = getRefBook();
        return dao.getRecordDataVersionWhere(rf, where, version);
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        Map<String, RefBookValue> value = dao.getRecordData(refBook, recordId);
        return value != null ? value.get(attribute.getAlias()) : null;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        PagingResult<Map<String, RefBookValue>> records = dao.getRecordVersionsByRecordId(getRefBook(), recordId, pagingParams, filter, sortAttribute);
        return commonRefBookService.dereference(getRefBook(), records);
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        return dao.getRecordVersionInfo(getRefBook(), uniqueRecordId);
    }

    @Override
    public List<Long> createRecordVersion(@NotNull Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {

        if (logger.getTaUserInfo() == null) {
            throw new ServiceException(CURRENT_USER_NOT_SET);
        }
        List<String> lockedObjects = new ArrayList<>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = commonRefBookService.generateTaskKey(getRefBook().getId());
        if (lockRefBook(getRefBook(), userId, lockKey)) {
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
            if (!getRefBook().isVersioned() && versionFrom == null) {
                versionFrom = new Date(0);
            }
            List<Long> excludedVersionEndRecords = new ArrayList<>();

            int countIds = 0;
            for (RefBookRecord record : records) {
                if (record.getRecordId() == null) {
                    countIds++;
                }
                record.setVersionTo(versionTo);
            }

            //Проверка корректности
            helper.checkCorrectness(logger, getRefBook(), null, versionFrom, records);

            if (getRefBook().isVersioned()) {
                for (RefBookRecord record : records) {
                    if (record.getRecordId() != null) {
                        List<CheckCrossVersionsResult> checkCrossVersionsResults =
                                dao.checkCrossVersions(getRefBook(), record.getRecordId(), versionFrom, record.getVersionTo(), null);
                        //Проверка пересечения версий
                        boolean needToCreateFakeVersion = helper.crossVersionsProcessing(checkCrossVersionsResults,
                                getRefBook(), versionFrom, record.getVersionTo(), logger);
                        if (!needToCreateFakeVersion) {
                            //Добавляем запись в список тех, для которых не будут созданы фиктивные версии
                            excludedVersionEndRecords.add(record.getRecordId());
                        }
                    }
                }
            }
            return helper.createVersions(refBook, versionFrom, versionTo, records, countIds, excludedVersionEndRecords, logger);
        } catch (DataAccessException e) {
            throw new ServiceException("Запись не сохранена. Обнаружены фатальные ошибки!", e);
        }
    }

    @Override
    public void updateRecordVersions(@NotNull Logger logger, Date versionFrom, Date versionTo, Set<Map<String, RefBookValue>> records) {
        checkIfRefBookIsEditable();
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException(CURRENT_USER_NOT_SET);
        }
        List<String> lockedObjects = new ArrayList<>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = commonRefBookService.generateTaskKey(getRefBook().getId());
        if (lockRefBook(getRefBook(), userId, lockKey)) {
            try {
                lockedObjects.add(lockKey);
                lockReferencedBooks(logger, lockedObjects);
                try {
                    for (Map<String, RefBookValue> record : records) {
                        updateRecordVersionWithoutLock(logger, record.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue(), versionFrom, versionTo, record);
                    }
                } catch (Exception e) {
                    ServiceLoggerException exception = new ServiceLoggerException(e.getLocalizedMessage(),
                            logEntryService.save(logger.getEntries()));
                    exception.initCause(e);
                    throw exception;
                }
            } finally {
                unlockObjects(lockedObjects, userId);
            }
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, getRefBook().getName()),
                    logEntryService.save(logger.getEntries()));
        }
    }

    @Override
    public void updateRecordVersion(@NotNull Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        checkIfRefBookIsEditable();
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException(CURRENT_USER_NOT_SET);
        }
        List<String> lockedObjects = new ArrayList<>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = commonRefBookService.generateTaskKey(getRefBook().getId());
        if (lockRefBook(getRefBook(), userId, lockKey)) {
            try {
                lockedObjects.add(lockKey);
                lockReferencedBooks(logger, lockedObjects);
                try {
                    updateRecordVersionWithoutLock(logger, uniqueRecordId, versionFrom, versionTo, records);
                } catch (Exception e) {
                    ServiceLoggerException exception = new ServiceLoggerException(e.getLocalizedMessage(),
                            logEntryService.save(logger.getEntries()));
                    exception.initCause(e);
                    throw exception;
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

    /**
     * Блокирует справочник для указанного пользователя, если тот же справочник уже был ранее заблокирован тем же пользователем ошибкой не считаем
     *
     * @param refBook справочник
     * @param userId  пользователь
     * @param lockKey ключ блокировки
     * @return флаг успешности установки блокировки
     */
    private boolean lockRefBook(RefBook refBook, int userId, String lockKey) {
        LockData lockData = lockService.lock(lockKey, userId,
                String.format(DescriptionTemplate.REF_BOOK_EDIT.getText(), refBook.getName()));
        return lockData == null || lockData.getUserId() == userId;
    }

    private void lockReferencedBooks(@NotNull Logger logger, List<String> lockedObjects) {
        int userId = logger.getTaUserInfo().getUser().getId();
        List<RefBookAttribute> attributes = getRefBook().getAttributes();
        for (RefBookAttribute attribute : attributes) {
            if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                String referenceLockKey = commonRefBookService.generateTaskKey(attribute.getRefBookId());
                if (!lockedObjects.contains(referenceLockKey)) {
                    if (lockRefBook(attributeRefBook, userId, referenceLockKey)) {
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
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> record) {
        checkIfRefBookIsEditable();
        try {
            //Получаем идентификатор записи справочника без учета версий
            Long recordId = dao.getRecordId(getRefBook(), uniqueRecordId);

            RefBookRecord refBookRecord = new RefBookRecord();
            refBookRecord.setUniqueRecordId(uniqueRecordId);
            refBookRecord.setRecordId(recordId);
            refBookRecord.setValues(record);
            refBookRecord.setVersionTo(versionTo);

            //Проверка корректности
            helper.checkCorrectness(logger, getRefBook(), uniqueRecordId, versionFrom, Arrays.asList(refBookRecord));

            RefBookRecordVersion previousVersion = null;
            if (getRefBook().isVersioned() && !(versionFrom == null && versionTo == null)) {
                //Получаем информацию о границах версии
                RefBookRecordVersion recordVersion = dao.getRecordVersionInfo(getRefBook(), uniqueRecordId);
                assert versionFrom != null;
                // Менялся ли период действия версии записи
                boolean isRelevancePeriodChanged = !versionFrom.equals(recordVersion.getVersionStart())
                        || (versionTo != null && !versionTo.equals(recordVersion.getVersionEnd()))
                        || (recordVersion.getVersionEnd() != null && !recordVersion.getVersionEnd().equals(versionTo));

                if (isRelevancePeriodChanged) {
                    //Проверка пересечения версий
                    //Проверяем следующую версию после даты окончания
                    RefBookRecordVersion nextRecordVersion = dao.getNextVersion(getRefBook(), recordId, recordVersion.getVersionStart());
                    if (versionTo != null && nextRecordVersion != null &&
                            (versionTo.equals(nextRecordVersion.getVersionStart()) || versionTo.after(nextRecordVersion.getVersionStart())) ||
                            versionTo == null && nextRecordVersion != null) {
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                    //Проверяем предыдущую версию до даты начала
                    previousVersion = dao.getPreviousVersion(getRefBook(), recordId, recordVersion.getVersionStart());
                    if (previousVersion != null &&
                            (versionFrom.equals(previousVersion.getVersionEnd())
                                    || versionFrom.before(previousVersion.getVersionEnd())
                                    || versionFrom.before(previousVersion.getVersionStart()))) {
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                }

                //Обновление периода актуальности
                if (isRelevancePeriodChanged) {

                    List<Long> uniqueIdAsList = Arrays.asList(uniqueRecordId);
                    if (previousVersion != null && (previousVersion.isVersionEndFake() && SimpleDateUtils.addDayToDate(previousVersion.getVersionEnd(), 1).equals(versionFrom))) {
                        //Если установлена дата окончания, которая совпадает с существующей фиктивной версией - то она удаляется
                        Long previousVersionEnd = dao.findRecord(getRefBook(), recordId, versionFrom);
                        refBookDao.deleteRecordVersions(getRefBook().getTableName(), Arrays.asList(previousVersionEnd), false);
                    }

                    boolean delayedUpdate = false;
                    if (versionFrom.equals(recordVersion.getVersionEnd())) {
                        //Обновляем дату начала актуальности, если не совпадает с датой окончания
                        refBookDao.updateVersionRelevancePeriod(getRefBook().getTableName(), uniqueRecordId, versionFrom);
                    } else {
                        delayedUpdate = true;
                    }

                    //Получаем запись - окончание версии. Если = null, то версия не имеет конца
                    List<Long> relatedVersions = dao.getRelatedVersions(getRefBook(), uniqueIdAsList);
                    if (relatedVersions.size() > 1) {
                        throw new ServiceException("Обнаружено несколько фиктивных версий");
                    }
                    if (versionTo != null) {
                        //Существует другая версия, дата начала которой = нашей новой дате окончания?
                        boolean isVersionEndAlreadyExists = dao.isVersionsExist(getRefBook(), Arrays.asList(recordId), SimpleDateUtils.addDayToDate(versionTo, 1));
                        if (relatedVersions.isEmpty() && !isVersionEndAlreadyExists) {
                            //Создаем новую фиктивную версию - дату окончания
                            dao.createFakeRecordVersion(getRefBook(), recordId, SimpleDateUtils.addDayToDate(versionTo, 1));
                        }

                        if (!relatedVersions.isEmpty() && !recordVersion.getVersionEnd().equals(versionTo)) {
                            if (!isVersionEndAlreadyExists) {
                                //Изменяем существующую дату окончания
                                refBookDao.updateVersionRelevancePeriod(getRefBook().getTableName(), relatedVersions.get(0), SimpleDateUtils.addDayToDate(versionTo, 1));
                            } else {
                                //Удаляем дату окончания. Теперь дата окончания задается началом следующей версии
                                refBookDao.deleteRecordVersions(getRefBook().getTableName(), relatedVersions, false);
                            }
                        }
                    }

                    if (!relatedVersions.isEmpty() && versionTo == null) {
                        //Удаляем фиктивную запись - теперь у версии нет конца
                        refBookDao.deleteRecordVersions(getRefBook().getTableName(), relatedVersions, false);
                    }

                    if (delayedUpdate) {
                        //Обновляем дату начала актуальности, если ранее это было отложено т.к она совпадала с датой окончания (теперь она изменена)
                        refBookDao.updateVersionRelevancePeriod(getRefBook().getTableName(), uniqueRecordId, versionFrom);
                    }
                }
            }

            //Обновление значений атрибутов версии
            dao.updateRecordVersion(getRefBook(), uniqueRecordId, record);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                ServiceLoggerException exception = new ServiceLoggerException("Запись не сохранена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
                exception.initCause(e);
                throw exception;
            } else {
                throw new ServiceException("Запись не сохранена, обнаружены фатальные ошибки!");
            }
        }

    }

    @Override
    public void deleteAllRecords(@NotNull Logger logger, List<Long> uniqueRecordIds) {
        checkIfRefBookIsEditable();
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        //Устанавливаем блокировку на текущий справочник
        List<String> lockedObjects = new ArrayList<>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = commonRefBookService.generateTaskKey(getRefBookId());
        if (lockRefBook(getRefBook(), userId, lockKey)) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                lockReferencedBooks(logger, lockedObjects);
                deleteAllRecordsWithoutLock(logger, uniqueRecordIds);
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

    private void checkIfRefBookIsEditable() {
        if (getRefBook().isReadOnly()) {
            throw new ServiceException("Справочник " + getRefBook().getName() + " предназначен только для чтения");
        }
    }

    @Override
    public void deleteAllRecordsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        checkIfRefBookIsEditable();
        try {
            //Ищем все ссылки на запись справочника без учета периода
            //Deprecated: helper.checkUsages(getRefBook(), uniqueRecordIds, null, null, null, logger, "Удаление невозможно, обнаружено использование элемента справочника!");
            dao.deleteAllRecordVersions(getRefBook(), uniqueRecordIds);
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
    public void deleteRecordVersions(@NotNull Logger logger, List<Long> uniqueRecordIds) {
        checkIfRefBookIsEditable();
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = commonRefBookService.generateTaskKey(getRefBookId());
        RefBook refBook = refBookDao.get(getRefBookId());
        if (lockRefBook(getRefBook(), userId, lockKey)) {
            try {
                lockedObjects.add(lockKey);
                lockReferencedBooks(logger, lockedObjects);
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
    public void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        checkIfRefBookIsEditable();
        try {
            RefBook refBook = refBookDao.get(getRefBookId());
            //Ищем все ссылки на запись справочника без учета периода
            //Deprecated: helper.checkUsages(refBook, uniqueRecordIds, null, null, null, logger, "Удаление невозможно, обнаружено использование элемента справочника!");
            if (refBook.isVersioned()) {
                List<Long> fakeVersionIds = dao.getRelatedVersions(getRefBook(), uniqueRecordIds);
                uniqueRecordIds.addAll(fakeVersionIds);
            }
            refBookDao.deleteRecordVersions(getRefBook().getTableName(), uniqueRecordIds, false);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                throw new ServiceLoggerException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!");
            }
        }
    }

    @Override
    public Long getRecordId(@NotNull Long uniqueRecordId) {
        return dao.getRecordId(getRefBook(), uniqueRecordId);
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDao.getInactiveRecords(getRefBook().getTableName(), recordIds);
    }

    @Override
    public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
        Map<Long, Map<String, RefBookValue>> recordData = getRecordData(new ArrayList<>(recordIds));
        String refBookAttributeAlias = refBook.getAttribute(attributeId).getAlias();
        Map<Long, RefBookValue> result = new HashMap<>();
        for (Map.Entry<Long, Map<String, RefBookValue>> record : recordData.entrySet()) {
            result.put(record.getKey(), record.getValue().get(refBookAttributeAlias));
        }
        return result;
    }

    public RefBook getRefBook() {
        if (refBook == null) {
            throw new IllegalArgumentException("refBook must be set");
        }
        return refBook;
    }

    public void setRefBook(RefBook refBook) {
        if (!isRefBookSupported(refBook)) {
            throw new IllegalArgumentException(String.format(MSG_REFBOOK_NOT_SUPPORTED, refBook.getName(), refBook.getId()));
        }
        this.refBook = refBook;
    }

    public Long getRefBookId() {
        return getRefBook().getId();
    }

    public void setRefBookId(long refBookId) {
        RefBook probableRefBook = refBookDao.get(refBookId);
        if (!isRefBookSupported(probableRefBook)) {
            throw new IllegalArgumentException(String.format(MSG_REFBOOK_NOT_SUPPORTED, probableRefBook.getName(), probableRefBook.getId()));
        }
        this.refBook = probableRefBook;
    }

    public void setRefBookId(RefBook.Id id) {
        setRefBookId(id.getId());
    }

    public boolean isRefBookSupported(RefBook refBook) {
        return true;
    }

    public boolean isRefBookSupported(long refBookId) {
        RefBook refBook = refBookDao.get(refBookId);
        return isRefBookSupported(refBook);
    }
}