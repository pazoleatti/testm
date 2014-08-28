package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome102Dao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.DepartmentType.*;

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

    private static final String FILTER_BY_DEPARTMENT = "DEPARTMENT_ID = %d";

    public static final Long REF_BOOK_ID = RefBookDepartmentDao.REF_BOOK_ID;
    private static final String ERROR_MESSAGE = "Подразделение не сохранено, обнаружены фатальные ошибки!";
    private static final String DEPARTMENT_TABLE_NAME = "DEPARTMENT";
    private static final String DEPARTMENT_TYPE_ATTRIBUTE = "TYPE";
    private static final String DEPARTMENT_NAME_ATTRIBUTE = "NAME";
    private static final String DEPARTMENT_PARENT_ATTRIBUTE = "PARENT_ID";
    private static final String DEPARTMENT_ACTIVE_NAME = "IS_ACTIVE";
    private static final String INCOME_PERIOD_ATTRIBUTE_NAME = "ACCOUNT_PERIOD_ID";
    private static final long INCOME_PERIOD_ATTRIBUTE_ID = 1072;
    private static final String WARN_MESSAGE_TARGET =
            "Внимание! Форма %s подразделения %s при сохранении будет являться приемником для формы %s подразделения %s, относящимся к разным территориальным банкам";
    private static final String WARN_MESSAGE_SOURCE =
            "Внимание! Форма %s подразделения %s при сохранении будет являться источником для формы %s подразделения %s, относящимся к разным территориальным банкам";

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
    private FormDataService formDataService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;
    @Autowired
    private FormTypeService formTypeService;
    @Autowired
    private DeclarationTypeService declarationTypeService;
    @Autowired
    private FormDataSearchService formDataSearchService;
    @Autowired
    private FormTemplateService formTemplateService;
    @Autowired
    private DeclarationTemplateService declarationTemplateService;
    @Autowired
    private RefBookIncome101 refBookIncome101;
    @Autowired
    private RefBookIncome102 refBookIncome102;
    @Autowired
    RefBookIncome101Dao refBookIncome101Dao;
    @Autowired
    RefBookIncome102Dao refBookIncome102Dao;
    @Autowired
    AuditService auditService;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private LockDataService lockService;


    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDepartmentDao.getRecords(pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter) {
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
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDao.getChildrenRecords(REF_BOOK_ID, DEPARTMENT_TABLE_NAME, parentRecordId, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        throw new UnsupportedOperationException();//return refBookDepartmentDao.getRowNum(recordId, filter, sortAttribute, isSortAscending);
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
    public List<Date> getVersions(Date startDate, Date endDate) {
        // версионирования нет, только одна версия
		return Arrays.asList(new Date(0));
	}

    @SuppressWarnings("unchecked")
    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(final Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return new PagingResult(new ArrayList<Map<String, RefBookValue>>(){{add(getRecordData(recordId));}}, 1);
    }

    @Override
    public void insertRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecords(TAUserInfo taUserInfo, Date version, List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(TAUserInfo taUserInfo, Date version) {
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
        Date d = new Date(0);
        version.setVersionStart(d);
        version.setVersionEnd(d);
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
        String lockKey = LockData.LOCK_OBJECTS.REF_BOOK.name() + "_" + refBook.getId();
        LockData lockData = lockService.lock(lockKey, userId, LockData.STANDARD_LIFE_TIME);
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getRefBookId());
                        String referenceLockKey = LockData.LOCK_OBJECTS.REF_BOOK.name() + "_" + attributeRefBook.getId();
                        LockData referenceLockData = lockService.lock(referenceLockKey, userId, LockData.STANDARD_LIFE_TIME);
                        if (referenceLockData == null) {
                            //Блокировка установлена
                            lockedObjects.add(referenceLockKey);
                        } else {
                            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                    logEntryService.save(logger.getEntries()));
                        }
                    }
                }

                Map<String, RefBookValue> refBookValueMap = records.get(0).getValues();
                checkCorrectness(logger, null, attributes, records);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException("Подразделение не создано, обнаружены фатальные ошибки!",
                            logEntryService.save(logger.getEntries()));
                Department newDepartment = new Department();
                newDepartment.setName(records.get(0).getValues().get(DEPARTMENT_NAME_ATTRIBUTE).getStringValue());
                if (logger.containsLevel(LogLevel.ERROR))
                    return new ArrayList<Long>(0);
                int depId = refBookDepartmentDao.create(refBookValueMap, attributes);
                int terrBankId = departmentService.getParentTB(depId) != null ? departmentService.getParentTB(depId).getId() : 0;
                createPeriods(depId, fromCode(refBookValueMap.get(DEPARTMENT_TYPE_ATTRIBUTE).getReferenceValue().intValue()),
                        terrBankId);
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException("Подразделение не создано, обнаружены фатальные ошибки!",
                            logEntryService.save(logger.getEntries()));

                logger.info("Подразделение создано");
                auditService.add(FormDataEvent.ADD_DEPARTMENT, logger.getTaUserInfo(), 0,
                        null, null, null, null,
                        String.format("Создано подразделение %s, значения атрибутов: %s",
                                refBookValueMap.get(DEPARTMENT_NAME_ATTRIBUTE).toString(),
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
    public List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long recordId) {
        return new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
    }

    //http://conf.aplana.com/pages/viewpage.action?pageId=11378355
    @SuppressWarnings("unchecked")
    @Override
    public void updateRecordVersion(Logger logger, final Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        if (logger == null) {
            throw new ServiceException("Logger не может быть null!");
        }
        if (logger.getTaUserInfo() == null) {
            throw new ServiceException("Текущий пользователь не установлен!");
        }
        List<String> lockedObjects = new ArrayList<String>();
        int userId = logger.getTaUserInfo().getUser().getId();
        String lockKey = LockData.LOCK_OBJECTS.REF_BOOK.name() + "_" + REF_BOOK_ID;
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        LockData lockData = lockService.lock(lockKey, userId, LockData.STANDARD_LIFE_TIME);
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getId());
                        String referenceLockKey = LockData.LOCK_OBJECTS.REF_BOOK.name() + "_" + attributeRefBook.getId();
                        LockData referenceLockData = lockService.lock(referenceLockKey, userId, LockData.STANDARD_LIFE_TIME);
                        if (referenceLockData == null) {
                            //Блокировка установлена
                            lockedObjects.add(referenceLockKey);
                        } else {
                            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                    logEntryService.save(logger.getEntries()));
                        }
                    }
                }
                final Department dep = departmentService.getDepartment(uniqueRecordId.intValue());
                Department parentDep = records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue() != null ?
                        departmentService.getDepartment(records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue().intValue())
                        : null;
                DepartmentType oldType = dep.getType();
                DepartmentType newType = fromCode(records.get(DEPARTMENT_TYPE_ATTRIBUTE).getReferenceValue().intValue());
                boolean isChangeType = oldType != newType;

                Department oldTb = departmentService.getParentTB(uniqueRecordId.intValue());
                int oldTBId = oldTb != null ? oldTb.getId() : 0;
                Department newTb =
                        records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue() != null ?
                                departmentService.getParentTB(records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue().intValue()) :
                                departmentService.getBankDepartment();
                int newTBId = newTb != null ? newTb.getId() : uniqueRecordId.intValue();
                boolean isChangeTB = oldTBId != 0 && oldTBId != newTBId;

                if (isChangeTB)
                    throw new ServiceLoggerException("Невозможно переместить подразделение в состав другого территориального банка!",
                            logEntryService.save(logger.getEntries()));

                if (isChangeType){
                    switch (oldType){
                        //3 шаг
                        case ROOT_BANK :
                            logger.error("Подразделению не может быть изменен тип \"Банк\"!\"");
                            throw new ServiceLoggerException(ERROR_MESSAGE,
                                    logEntryService.save(logger.getEntries()));
                            //4 шаг
                        case TERR_BANK:
                            List<ReportPeriod> openReportPeriods = new ArrayList<ReportPeriod>(0);
                            openReportPeriods.addAll(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.TRANSPORT, Arrays.asList(uniqueRecordId.intValue()), true, true));
                            openReportPeriods.addAll(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.PROPERTY, Arrays.asList(uniqueRecordId.intValue()), true, true));
                            if (!openReportPeriods.isEmpty()){
                                for (ReportPeriod period : openReportPeriods)
                                    logger.warn(
                                            "Для подразделения %s для налога %s уже открыт период %s для %d",
                                            dep.getName(),
                                            period.getTaxPeriod().getTaxType().getName(),
                                            period.getName(),
                                            period.getTaxPeriod().getYear());
                                throw new ServiceLoggerException("Подразделению не может быть изменен тип \"ТБ\", если для него существует период!",
                                        logEntryService.save(logger.getEntries()));
                            }
                            break;
                        //5 шаг
                        case CSKO_PCP:
                        case MANAGEMENT:
                            if (newType.equals(CSKO_PCP) || newType.equals(MANAGEMENT))
                                break;
                            List<TAUserFull> users = taUserService.getByFilter(new MembersFilterData(){{
                                setDepartmentIds(new HashSet<Integer>(Arrays.asList(uniqueRecordId.intValue())));
                            }});
                            if (!users.isEmpty()){
                                for (TAUserFull user : users)
                                    logger.error("Пользователь %s назначен подразделению %s", user.getUser().getName(), dep.getName());
                                throw new ServiceLoggerException("Невозможно изменить тип \"Управление\", если подразделению назначены пользователи!", logEntryService.save(logger.getEntries()));
                            }
                            break;
                    }
                }

                RefBookRecord refBookRecord = new RefBookRecord();
                refBookRecord.setRecordId(uniqueRecordId);
                refBookRecord.setValues(records);

                //Проверка корректности
                //6 шаг
                checkCorrectness(logger, uniqueRecordId, attributes, Arrays.asList(refBookRecord));
                if (logger.containsLevel(LogLevel.ERROR))
                    throw new ServiceLoggerException(ERROR_MESSAGE,
                            logEntryService.save(logger.getEntries()));

                //7
                if (versionFrom != null){
                    if (oldType != TERR_BANK){
                        //11А.3.1
                        formDataService.updateFDDepartmentNames(dep.getId(), records.get(DEPARTMENT_NAME_ATTRIBUTE).getStringValue(), versionFrom, versionTo);
                    }else {
                        //11А.3.1А
                        formDataService.updateFDTBNames(dep.getId(), records.get(DEPARTMENT_NAME_ATTRIBUTE).getStringValue(), versionFrom, versionTo);
                    }
                }

                //9 шаг. Проверка зацикливания
                if (dep.getType() != DepartmentType.ROOT_BANK && dep.getParentId() != null && dep.getParentId() != (parentDep != null ? parentDep.getId() : 0)){
                    checkCycle(dep, parentDep, logger);
                    if (logger.containsLevel(LogLevel.ERROR))
                        throw new ServiceLoggerException(ERROR_MESSAGE,
                                logEntryService.save(logger.getEntries()));
                }

                //Сохранение
                refBookDepartmentDao.update(uniqueRecordId.intValue(), records, refBook.getAttributes());

                auditService.add(FormDataEvent.UPDATE_DEPARTMENT, logger.getTaUserInfo(), uniqueRecordId.intValue(),
                        null, null, null, null,
                        String.format("Изменены значения атрибутов подразделения %s, новые значения атрибутов: %s",
                                departmentService.getDepartment(uniqueRecordId.intValue()).getName(),
                                assembleMessage(records)), null);
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
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
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
        String lockKey = LockData.LOCK_OBJECTS.REF_BOOK.name() + "_" + REF_BOOK_ID;
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        LockData lockData = lockService.lock(lockKey, userId, LockData.STANDARD_LIFE_TIME);
        if (lockData == null) {
            try {
                //Блокировка установлена
                lockedObjects.add(lockKey);
                //Блокируем связанные справочники
                List<RefBookAttribute> attributes = refBook.getAttributes();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE)) {
                        RefBook attributeRefBook = refBookDao.get(attribute.getId());
                        String referenceLockKey = LockData.LOCK_OBJECTS.REF_BOOK.name() + "_" + attribute.getRefBookId();
                        LockData referenceLockData = lockService.lock(referenceLockKey, userId, LockData.STANDARD_LIFE_TIME);
                        if (referenceLockData == null) {
                            //Блокировка установлена
                            lockedObjects.add(referenceLockKey);
                        } else {
                            throw new ServiceLoggerException(String.format(LOCK_MESSAGE, attributeRefBook.getName()),
                                    logEntryService.save(logger.getEntries()));
                        }
                    }
                }

                int depId = uniqueRecordIds.get(0).intValue();
                List<Integer> childIds = departmentService.getAllChildrenIds(depId);
                if (!childIds.isEmpty() && childIds.size() > 1){
                    logger.error("Обнаружены подчиненные подразделения для %s", departmentService.getDepartment(depId).getName());
                    throw new ServiceLoggerException("Подразделение не удалено!", logEntryService.save(logger.getEntries()));
                }
                isInUsed(departmentService.getDepartment(depId), logger);
                if (logger.containsLevel(LogLevel.ERROR) || logger.containsLevel(LogLevel.WARNING) && !force)
                    return;

                List<Long> income101Ids =  refBookIncome101.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, depId));
                if (!income101Ids.isEmpty())
                    refBookIncome101.deleteRecordVersions(logger, income101Ids, false);
                List<Long> income102Ids = refBookIncome102.getUniqueRecordIds(null,
                        String.format(FILTER_BY_DEPARTMENT, depId));
                if (!income102Ids.isEmpty())
                    refBookIncome102.deleteRecordVersions(logger, income102Ids, false);

                Collection<Long> dftIsd = CollectionUtils.collect(sourceService.getDFTByDepartment(depId, null, null, null),
                        new Transformer() {
                            @Override
                            public Object transform(Object o) {
                                return ((DepartmentFormType) o).getId();
                            }
                        });
                if (!dftIsd.isEmpty())
                    sourceService.deleteDFT(dftIsd);
                Collection<Long> ddtIds = CollectionUtils.collect(sourceService.getDDTByDepartment(depId, null, null, null),
                        new Transformer() {
                            @Override
                            public Object transform(Object o) {
                                return ((DepartmentDeclarationType) o).getId();
                            }
                        });
                if (!ddtIds.isEmpty())
                    sourceService.deleteDDT(ddtIds);
                //
                RefBookDataProvider provider = rbFactory.getDataProvider(RefBook.DEPARTMENT_CONFIG_INCOME);
                List<Long> uniqueIds = provider.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, depId));
                if (!uniqueIds.isEmpty()){
                    provider.deleteRecordVersions(logger, uniqueIds, false);
                }
                provider = rbFactory.getDataProvider(RefBook.DEPARTMENT_CONFIG_TRANSPORT);
                uniqueIds = provider.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, depId));
                if (!uniqueIds.isEmpty()){
                    provider.deleteRecordVersions(logger, uniqueIds, false);
                }
                provider = rbFactory.getDataProvider(RefBook.DEPARTMENT_CONFIG_DEAL);
                uniqueIds = provider.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, depId));
                if (!uniqueIds.isEmpty()){
                    provider.deleteRecordVersions(logger, uniqueIds, false);
                }
                provider = rbFactory.getDataProvider(RefBook.DEPARTMENT_CONFIG_VAT);
                uniqueIds = provider.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, depId));
                if (!uniqueIds.isEmpty()){
                    provider.deleteRecordVersions(logger, uniqueIds, false);
                }
                //Когда атрибуты появятся
                /*provider = rbFactory.getDataProvider(RefBook.DEPARTMENT_CONFIG_PROPERTY);
                uniqueIds = provider.getUniqueRecordIds(calendar.getTime(), String.format(FILTER_BY_DEPARTMENT, depId));
                if (!uniqueIds.isEmpty()){
                    provider.deleteRecordVersions(logger, uniqueIds, false);
                } */

                deletePeriods(depId);

                auditService.add(FormDataEvent.DELETE_DEPARTMENT, logger.getTaUserInfo(), 0, null, null, null, null,
                        String.format("Удалено подразделение %s", departmentService.getParentsHierarchy(depId)), null);
                refBookDepartmentDao.remove(depId);
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
    public List<Long> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDepartmentDao.isRecordsActiveInPeriod(recordIds, periodFrom, periodTo);
    }

    /**
     * Проверка корректности
     * @param recordId уникальный идентификатор записи
     * @param attributes атрибуты справочника
     * @param records значения справочника
     */
    private void checkCorrectness(Logger logger, Long recordId, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        Department rootBank = departmentService.getBankDepartment();
        Map<String, RefBookValue> values = records.get(0).getValues();
        DepartmentType type = values.get(DEPARTMENT_TYPE_ATTRIBUTE) != null ?
                DepartmentType.fromCode(values.get(DEPARTMENT_TYPE_ATTRIBUTE).getReferenceValue().intValue()) :
                null;
        Long parentDepartmentId = values.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue();
        if (parentDepartmentId != null &&
                type == DepartmentType.ROOT_BANK){
            logger.error("Подразделение с типом \"Банк\" не может иметь родительское подразделение!");
            return;
        }

        if (type != DepartmentType.ROOT_BANK && parentDepartmentId == null){
            logger.error("Для подразделения должен быть указан код родительского подразделения!");
            return;
        }
        if (rootBank != null && type == DepartmentType.ROOT_BANK && (recordId == null || rootBank.getId() != recordId.intValue())){
            logger.error("Подразделение с типом \"Банк\" уже существует!");
            return;
        }
        if (type != null && TERR_BANK.getCode() == type.getCode() &&
                parentDepartmentId != null && rootBank != null && parentDepartmentId.intValue() != rootBank.getId()){
            logger.error("Территориальный банк может быть подчинен только Банку!");
            return;
        }

        List<String> errors = RefBookUtils.checkFillRequiredRefBookAtributes(attributes, records);
        if (errors.size() > 0){
            throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
        }
        //Проверка корректности значений атрибутов
        errors = RefBookUtils.checkRefBookAtributeValues(attributes, records);
        if (!errors.isEmpty()){
            for (String error : errors) {
                logger.error(error);
            }
            throw new ServiceLoggerException("Обнаружено некорректное значение атрибута", logEntryService.save(logger.getEntries()));
        }

        //Получаем записи у которых совпали значения уникальных атрибутов
        List<Pair<String,String>> matchedRecords = refBookDepartmentDao.getMatchedRecordsByUniqueAttributes(recordId, attributes, records);
        if (matchedRecords != null && !matchedRecords.isEmpty()) {
            for (Pair<String,String> pair : matchedRecords) {
                logger.error(String.format("Нарушено требование к уникальности, уже существует подразделение %s с такими значениями атрибута \"%s\"!",
                        pair.getFirst(), pair.getSecond()));
            }
            throw new ServiceLoggerException(ERROR_MESSAGE, logEntryService.save(logger.getEntries()));
        }


        //Новое подразделение не имеет смысла проверять
        if (recordId == null) {
            //Если нет родительского то это подразделение Банк
            if (parentDepartmentId == null)
                return;
            //Проверяем аттрибут "действующее подразделение" у родительского подразделения
            Department parentDep = departmentService.getDepartment(parentDepartmentId.intValue());
            if (!parentDep.isActive() && values.get(DEPARTMENT_ACTIVE_NAME).getNumberValue().intValue() == 1){
                logger.error("Подразделению не может быть установлен признак \"Действующее\", если оно находится в составе недействующего подразделения!");
            }
        }else {
            Department currDepartment = departmentService.getDepartment(recordId.intValue());
            boolean isChangeActive = values.get(DEPARTMENT_ACTIVE_NAME).getNumberValue().intValue() != (currDepartment.isActive() ? 1 :0);
            if (!isChangeActive)
                return;
            if(values.get(DEPARTMENT_ACTIVE_NAME).getNumberValue().intValue() == 0){
                List<Department> childIds = departmentService.getAllChildren(recordId.intValue());
                for (Department child : childIds){
                    if (recordId != child.getId() && child.isActive()){
                        logger.error("Подразделение не может быть недействующим, если в его составе есть действующие подразделения!");
                        return;
                    }
                }
            }
            //Если нет родительского то это подразделение Банк
            if (parentDepartmentId == null)
                return;
            //Проверяем аттрибут "действующее подразделение" у родительского подразделения
            Department parentDep = departmentService.getDepartment(parentDepartmentId.intValue());
            if (!parentDep.isActive() && values.get(DEPARTMENT_ACTIVE_NAME).getNumberValue().intValue() == 1){
                logger.error("Подразделение не может быть действующим, если оно находится в составе недействующего подразделения!");
            }
        }

    }

    //http://conf.aplana.com/pages/viewpage.action?pageId=11402881
    private void createPeriods(long depId, DepartmentType newDepartmentType, int terrBankId){
        //1
        if (newDepartmentType != DepartmentType.TERR_BANK){
            if (departmentService.getParentTB((int) depId) != null){
                //1А.1.1
                List<DepartmentReportPeriod> listDRP =
                        periodService.getDRPByDepartmentIds(null, Arrays.asList((long) terrBankId));
                if (!listDRP.isEmpty()){
                    for (DepartmentReportPeriod drp : listDRP)
                        //1А.1.1.1
                        if (periodService.existForDepartment((int) depId, drp.getReportPeriod().getId()))
                            return;
                    //1А.1.1.1А
                    for (DepartmentReportPeriod drp : listDRP){
                        DepartmentReportPeriod drpCopy = new DepartmentReportPeriod();
                        drpCopy.setReportPeriod(drp.getReportPeriod());
                        drpCopy.setDepartmentId(depId);
                        drpCopy.setActive(drp.isActive());
                        drpCopy.setCorrectPeriod(drp.getCorrectPeriod());
                        drpCopy.setBalance(drp.isBalance());
                        periodService.saveOrUpdate(drpCopy, null, null);
                    }
                    return;
                }
                return;
            }
        }
        //2
        List<DepartmentReportPeriod> listDRP =
                periodService.getDRPByDepartmentIds(Arrays.asList(TaxType.INCOME, TaxType.DEAL, TaxType.VAT), Arrays.asList(0l));
        if (!listDRP.isEmpty()){
            for (DepartmentReportPeriod drp : listDRP)
                //1А.1.1.1
                if (periodService.existForDepartment((int) depId, drp.getReportPeriod().getId()))
                    return;
            for (DepartmentReportPeriod drp : listDRP){
                DepartmentReportPeriod drpCopy = new DepartmentReportPeriod();
                drpCopy.setReportPeriod(drp.getReportPeriod());
                drpCopy.setDepartmentId(depId);
                drpCopy.setActive(drp.isActive());
                drpCopy.setCorrectPeriod(null);
                drpCopy.setBalance(drp.isBalance());
                periodService.saveOrUpdate(drpCopy, null, null);
            }
        }
    }

    //Проверка использования
    private void isInUsed(final Department department, Logger logger){
        //1 точка запроса
        List<FormData> formDatas =
                formDataSearchService.findDataByFilter(new FormDataFilter(){{
                    setDepartmentIds(Arrays.asList(department.getId()));
                    setSearchOrdering(FormDataSearchOrdering.ID);
                    setFormDataKind(new ArrayList<Long>(0));
                }});
        for (FormData formData : formDatas){
            logger.error(String.format("Существует экземпляр формы %s типа %s в подразделении %s в периоде %s!",
                    formTemplateService.get(formData.getFormTemplateId()).getName(),
                    formData.getKind().getName(),
                    department.getName(),
                    periodService.getReportPeriod(formData.getReportPeriodId()).getName()));
        }

        //2 точка запроса
        List<DeclarationData> declarationDatas =
                declarationDataSearchService.getDeclarationData(new DeclarationDataFilter(){{setDepartmentIds(Arrays.asList(department.getId()));}},
                DeclarationDataSearchOrdering.ID, true);
        for (DeclarationData decData : declarationDatas){
            logger.error(String.format("Существует экземпляр декларации %s в подразделении %s в периоде %s!",
                    declarationTemplateService.get(decData.getDeclarationTemplateId()).getName(),
                    department.getName(),
                    periodService.getReportPeriod(decData.getReportPeriodId()).getName()));
        }

        //3 точка запроса
        for (String result : refBookDao.isVersionUsedInRefBooks(REF_BOOK_ID, Arrays.asList((long) department.getId()))){
            logger.error(result);
        }

        //4 точка запроса
        List<DepartmentFormType> departmentFormTypes = sourceService.getDFTByDepartment(department.getId(), null, null, null);
        for (DepartmentFormType dft : departmentFormTypes){
            FormType formType =  formTypeService.get(dft.getFormTypeId());
            logger.warn(String.format("Существует назначение формы %s типа %s подразделению %s!",
                    formType.getName(), dft.getKind().getName(), department.getName())
            );
        }

        //5 точка запроса
        List<DepartmentDeclarationType> departmentDeclarationTypes = sourceService.getDDTByDepartment(department.getId(), null, null, null);
        for (DepartmentDeclarationType ddt : departmentDeclarationTypes){
            DeclarationType declarationType = declarationTypeService.get(ddt.getDeclarationTypeId());
            logger.warn(String.format("Существует назначение декларации %s подразделению %s!",
                    declarationType.getName(), department.getName()));
        }

        //6 точка запроса
        List<Long> ref101 = refBookIncome101.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, department.getId()));
        List<Long> ref102 = refBookIncome102.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, department.getId()));
        for (Long id : ref101){
            logger.warn(String.format("Существует загруженная для подразделения %s бух. отчетность в периоде %s!",
                    department.getName(), refBookIncome101Dao.getPeriodNameFromRefBook(id)));
        }
        for (Long id : ref102){
            logger.warn(String.format("Существует загруженная для подразделения %s бух. отчетность в периоде %s!",
                    department.getName(), refBookIncome102Dao.getPeriodNameFromRefBook(id)));
        }

        //7 точка запроса
        List<TAUserFull> users = taUserService.getByFilter(new MembersFilterData(){{
            setDepartmentIds(new HashSet<Integer>(Arrays.asList(department.getId())));
        }});
        for (TAUserFull taUserFull : users)
            logger.error(String.format("Подразделению %s назначен пользовател с логином %s!", department.getName(), taUserFull.getUser().getName()));

        //8 точка запроса
        List<DepartmentFormType> departmentFormTypesDest = sourceService.getFormDestinations(department.getId(), 0, null, null, null);
        List<DepartmentDeclarationType> departmentDeclarationTypesDest = sourceService.getDeclarationDestinations(department.getId(), 0, null, null, null);
        List<DepartmentFormType> depFTSources = sourceService.getDFTSourcesByDFT(department.getId(), 0 , null, null, null);
        //List<DepartmentFormType> depDTSources = sourceService.getDFTSourceByDDT(department.getId(), 0);
        //TODO : Доделать после того как Денис сделает источники-приемники
        for (DepartmentFormType departmentFormType : departmentFormTypesDest){
            logger.warn(String.format("назначение является источником для %s - %s - %s приемника",
                    department.getName(),
                    departmentFormType.getKind().getName(),
                    formTypeService.get(departmentFormType.getFormTypeId()).getName()));
        }
        for (DepartmentDeclarationType departmentDeclarationType : departmentDeclarationTypesDest){
            logger.warn(String.format("назначение является источником для %s - %s приемника",
                    department.getName(),
                    declarationTypeService.get(departmentDeclarationType.getDeclarationTypeId()).getName()));
        }
        for (DepartmentFormType departmentFormType : depFTSources){
            logger.warn(String.format("назначение является приёмником для %s - %s - %s приемника",
                    department.getName(),
                    departmentFormType.getKind().getName(),
                    formTypeService.get(departmentFormType.getFormTypeId()).getName()));
        }

        //9 точка запроса
        ConfigurationParamModel model = configurationService.getByDepartment(department.getId(), logger.getTaUserInfo());
        if (!model.isEmpty())
            logger.warn("Заданы пути к каталогам транспортных файлов для %s!", department.getName());
    }

    private void deletePeriods(int depId){
        List<Long> reportPeriods =
                refBookDepartmentDao.getPeriodsByTaxTypesAndDepartments(
                        Arrays.asList(TaxType.values()),
                        Arrays.asList(depId));
        for (Long id : reportPeriods){
            periodService.removePeriodWithLog(id.intValue(), null, Arrays.asList(depId), null, null);
        }

    }

    private void checkCycle(Department department, Department parentDep, Logger logger){
        List<Integer> childIds = departmentService.getAllChildrenIds(department.getId());
        //>1 т.к. запрос всегда как минимум возвращает переданный id
        boolean isChild = !childIds.isEmpty() && childIds.size() > 1 && childIds.contains(department.getId());
        if (isChild)
            logger.error("Подразделение %s не может быть указано как родительское, т.к. входит в иерархию подчинённости подразделения %s",
                    parentDep.getName(), department.getName());

        /*List<Integer> parentIds = departmentService.getAllParentIds(department.getId());
        //>2 т.к. запрос всегда как минимум возвращает переданный id и подразделение Банк
        boolean isParent = !parentIds.isEmpty() && parentIds.size() > 2 && parentIds.contains(department.getId());
        if (isParent)
            logger.error("Подразделение %s не может быть включено в орг. структуру подразделения %s, т.к. уже содержит его в своей орг. структуре!",
                    parentDep.getName(), department.getName());*/
    }

    private String assembleMessage(Map<String, RefBookValue> records){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, RefBookValue> record : records.entrySet()){
            if (!record.getKey().equals(DEPARTMENT_NAME_ATTRIBUTE))
                sb.append(String.format(" %s- %s ", record.getKey(), record.getValue().toString()));
        }

        return sb.toString();
    }
}
