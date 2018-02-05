package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.DepartmentType.fromCode;

/**
 * Провайдер для работы со справочником подразделений
 * В текущей версии расчитано что он будет использоваться только  для получение данных, возможности вставки данных НЕТ, версионирования НЕТ (данные в одном экземпляре)
 * Смотри http://jira.aplana.com/browse/SBRFACCTAX-3245
 * User: ekuvshinov
 */
@Service("refBookDepartment")
@Transactional
public class RefBookDepartment implements RefBookDataProvider {

    private static final String FILTER_BY_DEPARTMENT = "DEPARTMENT_ID = %d";

    public static final Long REF_BOOK_ID = RefBookDepartmentDao.REF_BOOK_ID;
    private static final String ERROR_MESSAGE = "Подразделение не может быть отредактировано, так как были обнаружены фатальные ошибки!";
    private static final String ERROR_MESSAGE_CREATE =  "Подразделение не может быть создано, обнаружены фатальные ошибки!";
    private static final String DEPARTMENT_TABLE_NAME = "DEPARTMENT";
    private static final String DEPARTMENT_TYPE_ATTRIBUTE = "TYPE";
    private static final String DEPARTMENT_NAME_ATTRIBUTE = "NAME";
    private static final String DEPARTMENT_PARENT_ATTRIBUTE = "PARENT_ID";
    private static final String DFT_RELATION =
            "В настройке подразделения %s для налога %s в периоде %s %s указана ссылка на версию!";

    private static final ThreadLocal<SimpleDateFormat> SDF_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy");
        }
    };

    @Autowired
    RefBookDao refBookDao;
    @Autowired
    private RefBookDepartmentDao refBookDepartmentDao;
    @Autowired
    private RefBookUtils refBookUtils;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private PeriodService periodService;
    @Autowired
    private TAUserService taUserService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    AuditService auditService;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private LockDataService lockService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDepartmentDao.getRecords(pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

	@Override
	public PagingResult<Map<String, RefBookValue>> getRecordsVersion(Date versionFrom, Date versionTo, PagingParams pagingParams, String filter) {
		return getRecords(versionTo, pagingParams, filter, null);
	}

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNextVersion(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getEndVersion(Long recordId, Date versionFrom) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookDepartmentDao.getRecordsCount(filter);
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> isRecordsExist(List<Long> uniqueRecordIds) {
        return refBookDepartmentDao.isRecordsExist(uniqueRecordIds);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDao.getChildrenRecords(REF_BOOK_ID, DEPARTMENT_TABLE_NAME, parentRecordId, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        return refBookUtils.getParentsHierarchy(DEPARTMENT_TABLE_NAME, uniqueRecordId);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookDepartmentDao.getRecordData(recordId);
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
    public List<Date> getVersions(Date startDate, Date endDate) {
        // версионирования нет, только одна версия
		return Arrays.asList(new Date(0));
	}

    @SuppressWarnings("unchecked")
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsById(final Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return new PagingResult(new ArrayList<Map<String, RefBookValue>>(){{add(getRecordData(recordId));}}, 1);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
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
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return refBookDepartmentDao.getRecordData(recordId).get(attribute.getAlias());
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
    public int getRecordVersionsCount(Long refBookRecordId) {
        return 1;
    }

    @Override
    public List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        String lockKey = refBookFactory.generateTaskKey(REF_BOOK_ID);
        LockData lockData = lockService.lock(lockKey, userId, String.format(DescriptionTemplate.REF_BOOK_EDIT.getText(), refBook.getName()));
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = refBookFactory.generateTaskKey(attribute.getRefBookId());
                        if (!lockedObjects.contains(referenceLockKey)) {
                            if (lockService.lock(referenceLockKey, userId, String.format(DescriptionTemplate.REF_BOOK_EDIT.getText(), attributeRefBook.getName())) == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }

                Map<String, RefBookValue> refBookValueMap = records.get(0).getValues();
                checkCorrectness(logger, attributes, records);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException(ERROR_MESSAGE_CREATE,
                            logEntryService.save(logger.getEntries()));
                if (logger.containsLevel(LogLevel.ERROR))
                    return new ArrayList<Long>(0);
                int depId = refBookDepartmentDao.create(refBookValueMap, attributes);
                int terrBankId = departmentService.getParentTB(depId) != null ? departmentService.getParentTB(depId).getId() : 0;
                createPeriods(depId, fromCode(refBookValueMap.get(DEPARTMENT_TYPE_ATTRIBUTE).getReferenceValue().intValue()),
                        terrBankId);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException(ERROR_MESSAGE_CREATE,
                            logEntryService.save(logger.getEntries()));

                logger.info("Подразделение создано");
                auditService.add(FormDataEvent.ADD_DEPARTMENT, logger.getTaUserInfo(), null,
                        null, null, null, null,
                        String.format("Создано подразделение %s, значения атрибутов: %s",
                                departmentService.getParentsHierarchy(depId),
                                assembleMessage(refBookValueMap)), null);
                return Arrays.asList((long)depId);
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
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long recordId) {
        return new HashMap<Integer, List<Pair<RefBookAttribute, RefBookValue>>>();
    }

    @Override
    public void updateRecordVersions(Logger logger, Date versionFrom, Date versionTo, Set<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    //http://conf.aplana.com/pages/viewpage.action?pageId=11378355
    //После изменения в 0.7 метод для подразеделений делает ТОЛЬКО проверку, без сохранения
    //Сохранение происходит в UnitEditingHandler
    @SuppressWarnings("unchecked")
    @Override
    public void updateRecordVersion(Logger logger, final Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }

        RefBook refBook = refBookDao.get(REF_BOOK_ID);

        if (lockService.getLock(refBookFactory.generateTaskKey(RefBookDepartmentDao.REF_BOOK_ID)) == null) {
            List<RefBookAttribute> attributes = refBook.getAttributes();
            final Department dep = departmentService.getDepartment(uniqueRecordId.intValue());
            // проверка использования подразделения в модуле гарантий
                /*if (dep.isGarantUse()) {
                    logger.error("Подразделение используется в АС \"Гарантии\"");
                    throw new ServiceLoggerException("Подразделение не может быть отредактировано!",
                            logEntryService.save(logger.getEntries()));
                }*/
            Department parentDep = records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue() != null ?
                    departmentService.getDepartment(records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue().intValue())
                    : null;

            RefBookRecord refBookRecord = new RefBookRecord();
            refBookRecord.setUniqueRecordId(uniqueRecordId);
            refBookRecord.setValues(records);

            //Проверка корректности
            //6 шаг
            checkCorrectness(logger, attributes, Arrays.asList(refBookRecord));
            if (logger.containsLevel(LogLevel.ERROR))
                throw new ServiceLoggerException(ERROR_MESSAGE,
                        logEntryService.save(logger.getEntries()));

            //7 Перенесен в отдельный хендлер
                /*if (versionFrom != null){
                    if (oldType != TERR_BANK){
                        //Обновляем имена подразделений в печатных формах
                        formDataService.updateFDDepartmentNames(dep.getId(), records.get(DEPARTMENT_NAME_ATTRIBUTE).getStringValue(), versionFrom, versionTo, logger.getTaUserInfo());
                    }else {
                        //Обновляем имена ТБ в печатных формах
                        formDataService.updateFDTBNames(dep.getId(), records.get(DEPARTMENT_NAME_ATTRIBUTE).getStringValue(), versionFrom, versionTo, oldType == TERR_BANK && isChangeType, logger.getTaUserInfo());
                    }
                }*/

            //9 шаг. Проверка зацикливания
            if (dep.getType() != DepartmentType.ROOT_BANK && dep.getParentId() != null && dep.getParentId() != (parentDep != null ? parentDep.getId() : 0)){
                checkCycle(dep, parentDep, logger);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException(ERROR_MESSAGE, logEntryService.save(logger.getEntries()));
            }

            //Сохранение - пренесли в EditPrintFDHandler из-за http://jira.aplana.com/browse/SBRFACCTAX-11960
                /*refBookDepartmentDao.update(uniqueRecordId.intValue(), records, refBook.getAttributes());
                logger.info("Подразделение сохранено");

                auditService.add(FormDataEvent.UPDATE_DEPARTMENT, logger.getTaUserInfo(), uniqueRecordId.intValue(),
                        null, null, null, null,
                        String.format("Изменены значения атрибутов подразделения %s, новые значения атрибутов: %s",
                                departmentService.getDepartment(uniqueRecordId.intValue()).getName(),
                                assembleMessage(records)), null);*/
        } else {
            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, refBook.getName()),
                    logEntryService.save(logger.getEntries()));
        }
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

    @SuppressWarnings("unchecked")
    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = refBookFactory.generateTaskKey(REF_BOOK_ID);
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        if (lockService.lock(lockKey, userId,
                String.format(DescriptionTemplate.REF_BOOK_EDIT.getText(), refBook.getName())) == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = refBookFactory.generateTaskKey(attribute.getRefBookId());
                        if (!lockedObjects.contains(referenceLockKey)) {
                            if (lockService.lock(referenceLockKey, userId, String.format(DescriptionTemplate.REF_BOOK_EDIT.getText(), attributeRefBook.getName())) == null) {
                                //Блокировка установлена
                                lockedObjects.add(referenceLockKey);
                            } else {
                                throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                        logEntryService.save(logger.getEntries()));
                            }
                        }
                    }
                }

                int depId = uniqueRecordIds.get(0).intValue();
                Department department = departmentService.getDepartment(depId);

                // Проверка типа подразделения
                if (department.getType().equals(DepartmentType.ROOT_BANK)) {
                    throw new ServiceLoggerException("Подразделение не может быть удалено, так как оно имеет тип \"Банк\"!", null);
                }

                List<Integer> childIds = departmentService.getAllChildrenIds(depId);
                if (!childIds.isEmpty() && childIds.size() > 1) {
                    throw new ServiceLoggerException(
                            "Подразделение не может быть удалено, так как обнаружены подчиненные подразделения для %s!",
                            logEntryService.save(logger.getEntries()),
                            departmentService.getDepartment(depId).getName());
                }
                // проверка использования подразделения в гарантиях
                if (department.isGarantUse()) {
                    throw new ServiceLoggerException(
                            "Подразделение не может быть удалено, так как оно используется в АС \"Гарантии\"!",
                            null);
                }
                // проверка использования подразделения в СУНР
                if (department.isSunrUse()) {
                    throw new ServiceLoggerException(
                            "Подразделение не может быть удалено, так как оно используется в АС СУНР!",
                            null);
                }

                isInUsed(department, logger);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException(
                            "Подразделение не может быть удалено, так как обнаружены ссылки на подразделение!",
                            logEntryService.save(logger.getEntries())
                    );
                else if (logger.containsLevel(LogLevel.WARNING) && !force){
                    return;
                }

                //удаление настроек подразделений
                RefBookDataProvider provider;
                List<Long> uniqueIds;
                for(RefBook.WithTable withTable: RefBook.WithTable.values()) {
                    provider = rbFactory.getDataProvider(withTable.getRefBookId());
                    uniqueIds = provider.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, depId));
                    if (!uniqueIds.isEmpty()) {
                        provider.deleteRecordVersions(logger, uniqueIds, false);
                    }
                    provider = rbFactory.getDataProvider(withTable.getTableRefBookId());
                    uniqueIds = provider.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, depId));
                    if (!uniqueIds.isEmpty()) {
                        provider.deleteRecordVersions(logger, uniqueIds, false);
                    }
                }
                //удаление ссылок
                if (deleteReference(new RefBookAttribute(){{
						setRefBookId(RefBook.Id.DEPARTMENT.getId());
						setId(160L);
					}}, uniqueRecordIds.get(0), logger)) {
                    throw new ServiceLoggerException(
                            "Подразделение не может быть удалено, так как обнаружены ссылки на подразделение!",
                            logEntryService.save(logger.getEntries())
                    );
                }

                //удаление периодов
                deleteDRPs(depId);

                auditService.add(FormDataEvent.DELETE_DEPARTMENT, logger.getTaUserInfo(), null, null, null, null, null,
                        String.format("Удалено подразделение %s", departmentService.getParentsHierarchy(depId)), null);
                refBookDepartmentDao.remove(depId);
                logger.info("Подразделение удалено!");
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

    private boolean deleteReference(RefBookAttribute refBookAttribute, Long referenceId, Logger logger) {
        boolean used = false;
        //Проверка использования в нф
        RefBook refBook = refBookDao.getByAttribute(refBookAttribute.getId());
        RefBookDataProvider provider = refBookFactory.getDataProvider(refBook.getId());
        List<RefBookAttribute> attributeList = refBookDao.getAttributesByReferenceId(refBookAttribute.getRefBookId());
        for(RefBookAttribute referenceRefBookAttribute: attributeList) {
            RefBook refBookReference = refBookDao.getByAttribute(referenceRefBookAttribute.getId());
            if (refBookReference.getTableName() == null || refBookReference.getTableName().isEmpty()) {
                List<Long> recordIds = refBookDao.getDeletedRecords(refBookReference.getId(), referenceRefBookAttribute.getAlias() + "=" + referenceId);
                for (Long recordId : recordIds) {
                    used = used || deleteReference(referenceRefBookAttribute, recordId, logger);
                }
            }
        }
        if (!used && refBookAttribute.getAlias() != null) {
            refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, Arrays.asList(referenceId), true);
        }
        return used;
    }

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
       return uniqueRecordId;
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        return refBookDepartmentDao.getAttributesValues(attributePairs);
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDepartmentDao.getInactiveRecords(recordIds);
    }

    /**
     * Проверка корректности
     * @param attributes атрибуты справочника
     * @param records значения справочника
     */
    private void checkCorrectness(Logger logger, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        /*Map<String, RefBookValue> values = records.get(0).getValues();
        DepartmentType type = values.get(DEPARTMENT_TYPE_ATTRIBUTE) != null ?
                DepartmentType.fromCode(values.get(DEPARTMENT_TYPE_ATTRIBUTE).getReferenceValue().intValue()) :
                null;
        Long parentDepartmentId = values.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue();*/

        // большинство проверок перенесены в скрипт подразделения на событие SAVE

        //Проверка корректности значений атрибутов
        List<String> errors = RefBookUtils.checkRefBookAtributeValues(attributes, records);
        if (!errors.isEmpty()){
            for (String error : errors) {
                logger.error(error);
            }
        }
    }

    //http://conf.aplana.com/pages/viewpage.action?pageId=11402881
    private void createPeriods(int depId, DepartmentType newDepartmentType, int terrBankId){
        //1
        if (newDepartmentType != DepartmentType.TERR_BANK){
            if (departmentService.getParentTB(depId) != null){
                //1А.1.1
                List<DepartmentReportPeriod> listDRP =
                        periodService.getDRPByDepartmentIds(Arrays.asList(terrBankId));
                if (!listDRP.isEmpty()){
                    for (DepartmentReportPeriod drp : listDRP)
                        //1А.1.1.1
                        if (periodService.existForDepartment(depId, drp.getReportPeriod().getId()))
                            return;
                    //1А.1.1.1А
                    for (DepartmentReportPeriod drp : listDRP){
                        DepartmentReportPeriod drpCopy = new DepartmentReportPeriod();
                        drpCopy.setReportPeriod(drp.getReportPeriod());
                        drpCopy.setDepartmentId(depId);
                        drpCopy.setIsActive(drp.isActive());
                        drpCopy.setCorrectionDate(drp.getCorrectionDate());
                        periodService.saveOrOpen(drpCopy, null);
                    }
                    return;
                }
                return;
            }
        }
        //2
        List<DepartmentReportPeriod> listDRP =
                periodService.getDRPByDepartmentIds(Arrays.asList(0));
        if (!listDRP.isEmpty()){
            for (DepartmentReportPeriod drp : listDRP)
                //1А.1.1.1
                if (periodService.existForDepartment(depId, drp.getReportPeriod().getId()))
                    return;
            for (DepartmentReportPeriod drp : listDRP){
                DepartmentReportPeriod drpCopy = new DepartmentReportPeriod();
                drpCopy.setReportPeriod(drp.getReportPeriod());
                drpCopy.setDepartmentId(depId);
                drpCopy.setIsActive(drp.isActive());
                drpCopy.setCorrectionDate(drp.getCorrectionDate());
                periodService.saveOrOpen(drpCopy, null);
            }
        }
    }

    //Проверка использования
    private void isInUsed(final Department department, Logger logger){
        //2 точка запроса
        List<DeclarationData> declarationDatas =
                declarationDataSearchService.getDeclarationData(new DeclarationDataFilter(){{setDepartmentIds(Arrays.asList(department.getId()));}},
                DeclarationDataSearchOrdering.ID, true);
        for (DeclarationData decData : declarationDatas){
            logger.error(String.format("Существует экземпляр налоговой формы \"%s\" в подразделении \"%s\" в периоде \"%s\"!",
                    declarationTemplateService.get(decData.getDeclarationTemplateId()).getName(),
                    department.getName(),
                    periodService.fetchReportPeriod(decData.getReportPeriodId()).getName()));
        }

        //3 точка запроса
        for (String result : refBookDao.isVersionUsedInRefBooks(
                REF_BOOK_ID, Arrays.asList((long) department.getId()), null, null, null, Arrays.asList(31l, 33l, 37l, 98l, 99l))) {
            logger.error(result);
        }

        //5 точка запроса
        List<DepartmentDeclarationType> departmentDeclarationTypes = sourceService.getDDTByDepartment(department.getId(), null, null, null);
        for (DepartmentDeclarationType ddt : departmentDeclarationTypes){
            DeclarationType declarationType = declarationTypeService.get(ddt.getDeclarationTypeId());
            logger.warn(String.format("Существует назначение налоговой формы \"%s\" подразделению \"%s\"!",
                    declarationType.getName(), department.getName()));
        }

        //8 точка запроса
        List<TAUserView> users = taUserService.getUsersByFilter(new MembersFilterData() {{
            setDepartmentIds(new HashSet<Integer>(Arrays.asList(department.getId())));
        }});
        for (TAUserView taUser : users)
            logger.error(String.format("Подразделению %s назначен пользователь с логином %s!", department.getName(), taUser.getName()));

        //9 точка запроса Источники-приёмники
        List<DepartmentDeclarationType> departmentDeclarationTypesDest = sourceService.getDeclarationDestinations(department.getId(), 0, null, null, null);

        for (DepartmentDeclarationType departmentDeclarationType : departmentDeclarationTypesDest){
            logger.warn(String.format("назначение является источником для %s - %s приемника",
                    department.getName(),
                    declarationTypeService.get(departmentDeclarationType.getDeclarationTypeId()).getName()));
        }

        //10 точка запроса
        ConfigurationParamModel model = configurationService.fetchAllByDepartment(department.getId(), logger.getTaUserInfo());
        if (!model.isEmpty())
            logger.warn("Заданы пути к каталогам транспортных файлов для %s!", department.getName());

        //11 точка запроса
        Map<Integer, Map<String, Object>> records =
                refBookDepartmentDao.isVersionUsedInRefBooks(
                        Arrays.asList(RefBook.Id.NDFL.getId()),
                        Arrays.asList((long) department.getId())
                );
        for (Map.Entry<Integer, Map<String, Object>> entry : records.entrySet()){
			TaxType taxType = TaxType.NDFL;
            Date startDate = (Date)entry.getValue().get(RefBookDepartmentDao.VERSION_START_ALIAS);
            String rpName =
                    refBookDepartmentDao.getReportPeriodNameByDate(taxType, startDate);
            logger.error(
                    DFT_RELATION,
                    departmentService.getDepartment(department.getId()).getName(),
                    taxType.getName(),
                    rpName,
                    SDF_YYYY.get().format(startDate));
        }
    }

    private void deleteDRPs(int depId){
        DepartmentReportPeriodFilter drpFilter = new DepartmentReportPeriodFilter();
        drpFilter.setIsCorrection(null);
        drpFilter.setDepartmentIdList(Arrays.asList(depId));
        List<Integer> corrIds = departmentReportPeriodService.fetchAllIdsByFilter(drpFilter);
        departmentReportPeriodService.delete(corrIds);
    }

    private void checkCycle(Department department, Department parentDep, Logger logger){
        List<Integer> childIds = departmentService.getAllChildrenIds(department.getId());
        //>1 т.к. запрос всегда как минимум возвращает переданный id
        boolean isChild = !childIds.isEmpty() && childIds.size() > 1 && childIds.contains(parentDep.getId());
        if (isChild) {
            logger.error("Подразделение %s не может быть указано как родительское, т.к. входит в иерархию подчинённости подразделения %s",
                    parentDep.getName(), department.getName());
        }
    }

    private String assembleMessage(Map<String, RefBookValue> records){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, RefBookValue> record : records.entrySet()){
            if (!record.getKey().equals(DEPARTMENT_NAME_ATTRIBUTE))
                sb.append(String.format(" %s- %s ", record.getKey(), record.getValue().toString()));
        }

        return sb.toString();
    }

	@Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
		return refBookDao.dereferenceValues(DEPARTMENT_TABLE_NAME, attributeId, recordIds);
	}

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }

}
