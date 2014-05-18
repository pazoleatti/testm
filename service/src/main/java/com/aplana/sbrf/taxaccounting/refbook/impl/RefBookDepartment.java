package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public static final Long REF_BOOK_ID = RefBookDepartmentDao.REF_BOOK_ID;
    private static final String DEPARTMENT_TABLE_NAME = "DEPARTMENT";
    private static final String DEPARTMENT_TYPE_ATTRIBUTE = "TYPE";
    private static final String DEPARTMENT_PARENT_ATTRIBUTE = "PARENT_ID";
    private static final String WARN_MESSAGE =
            "Внимание! Форма %s подразделения %s при сохранении будет являться приемником для формы %s подразделения %s, относящимся к разным территориальным банкам";

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


    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDepartmentDao.getRecords(pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookUtils.getChildrenRecords(REF_BOOK_ID, DEPARTMENT_TABLE_NAME, parentRecordId, pagingParams, filter, sortAttribute, true);
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
    public List<Date> getVersions(Date startDate, Date endDate) {
        // версионирования нет, только одна версия
		return Arrays.asList(new Date[]{new Date(0)});
	}

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(final Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return new PagingResult(new ArrayList<Map<String, RefBookValue>>(){{add(getRecordData(recordId));}}, 1);
    }

    @Override
    public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Date version) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long recordId) {
        return new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
    }

    @Override
    public void updateRecordVersion(Logger logger, final Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        final Department dep = departmentService.getDepartment(uniqueRecordId.intValue());
        DepartmentType oldType = dep.getType();
        DepartmentType newType = fromCode(records.get(DEPARTMENT_TYPE_ATTRIBUTE).getNumberValue().intValue());
        boolean isChangeType = oldType != newType;
        boolean isHasOpenPeriods = false;
        if (isChangeType){
            switch (oldType){
                case ROOT_BANK :
                    logger.warn("Подразделению не может быть изменен тип \"Банк\"!\"");
                    return;
                case TERR_BANK:
                    List<ReportPeriod> openReportPeriods =
                            new ArrayList<ReportPeriod>(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.TRANSPORT, Arrays.asList(uniqueRecordId.intValue()), true, true));
                    openReportPeriods.addAll(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.PROPERTY, Arrays.asList(uniqueRecordId.intValue()), true, true));
                    isHasOpenPeriods = !openReportPeriods.isEmpty();
                    if (isHasOpenPeriods){
                        for (ReportPeriod period : openReportPeriods)
                            logger.warn("Для подразделения %s для налога <вид налога> открыт период %s", dep.getName(), period.getName());
                        throw new ServiceLoggerException("Подразделению не может быть изменен тип \"ТБ\", если для него существует период!",
                                logEntryService.save(logger.getEntries()));
                    }
                    break;
                case CSKO_PCP:
                case INTERNAL:
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

        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        List<RefBookAttribute> attributes = refBook.getAttributes();
        RefBookRecord refBookRecord = new RefBookRecord();
        refBookRecord.setRecordId(uniqueRecordId);
        refBookRecord.setValues(records);

        //Проверка корректности
        if (isHasOpenPeriods){
            checkCorrectness(logger, attributes, Arrays.asList(refBookRecord));
            if (logger.containsLevel(LogLevel.ERROR))
                throw new ServiceLoggerException("Подразделение не сохранено, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
        }

        //7
        int oldTBId = departmentService.getParentTB(uniqueRecordId.intValue()).getId();
        int newTBId = records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue().intValue() != 0?
                departmentService.getParentTB(records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue().intValue()).getId()
                : uniqueRecordId.intValue();
        if (oldTBId != newTBId){
            //7A.1
            if (!formDataService.existFormDataByTaxAndDepartment(Arrays.asList(TaxType.PROPERTY, TaxType.TRANSPORT), Arrays.asList(dep.getId())) ||
                    declarationDataSearchService.getDeclarationIds(
                            new DeclarationDataFilter(){{
                                setTaxType(TaxType.PROPERTY);
                                setDepartmentIds(Arrays.asList(dep.getId()));
                            }},
                    DeclarationDataSearchOrdering.ID, true).isEmpty() ||  declarationDataSearchService.getDeclarationIds(
                    new DeclarationDataFilter(){{
                        setTaxType(TaxType.TRANSPORT);
                        setDepartmentIds(Arrays.asList(dep.getId()));
                    }},
                    DeclarationDataSearchOrdering.ID, true).isEmpty()){
                //7A 1.1
                List<FormType> formTypes = sourceService.listAllByTaxType(TaxType.PROPERTY);
                formTypes.addAll(sourceService.listAllByTaxType(TaxType.PROPERTY));
                List<DepartmentFormType> departmentFormTypes = sourceService.getDestinationsFormWithDestDepartment(newTBId, oldTBId, formTypes);
                List<DepartmentDeclarationType> departmentDeclarationTypes = sourceService.getDestinationsDeclarationWithDestDepartment(newTBId, oldTBId, formTypes);
                if (departmentFormTypes != null && !departmentFormTypes.isEmpty() ||
                        departmentDeclarationTypes != null && !departmentDeclarationTypes.isEmpty()){
                    for (DepartmentFormType departmentFormType : departmentFormTypes){
                        logger.warn(WARN_MESSAGE);
                    }
                    createPeriods(dep);
                } else {
                    createPeriods(dep);
                }
            }
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

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
       return uniqueRecordId;
    }

    private void checkCorrectness(Logger logger, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        if (departmentService.getBankDepartment() != null){
            logger.error("Подразделение с типом \"Банк\" уже существует!");
            return;
        }
        if (records.get(0).getValues().get("TYPE") != null &&
                Integer.valueOf(TERR_BANK.getCode()).equals(records.get(0).getValues().get(DEPARTMENT_TYPE_ATTRIBUTE).getNumberValue())){
            logger.error("Территориальный банк может быть подчинен только Банку!");
            return;
        }

        List<String> errors = refBookUtils.checkFillRequiredRefBookAtributes(attributes, records);
        if (errors.size() > 0){
            throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
        }
        //Проверка корректности значений атрибутов
        errors = refBookUtils.checkRefBookAtributeValues(attributes, records);
        if (!errors.isEmpty()){
            for (String error : errors) {
                logger.error(error);
            }
            throw new ServiceLoggerException("Обнаружено некорректное значение атрибута", logEntryService.save(logger.getEntries()));
        }

        //Получаем записи у которых совпали значения уникальных атрибутов
        List<Pair<Long,String>> matchedRecords = refBookDao.getMatchedRecordsByUniqueAttributesForNonVersion(REF_BOOK_ID, DEPARTMENT_TABLE_NAME, attributes, records);
        if (matchedRecords != null && !matchedRecords.isEmpty()) {
            StringBuilder attrNames = new StringBuilder();
            for (Pair<Long,String> pair : matchedRecords) {
                attrNames.append("\"").append(pair.getSecond()).append("\", ");
            }
            attrNames.delete(attrNames.length() - 2, attrNames.length());
            throw new ServiceException(String.format("Нарушено требование к уникальности, уже существует подразделение %s с такими значениями атрибутов %s!",
                    departmentService.getDepartment(matchedRecords.get(0).getFirst().intValue()).getSbrfCode(),
                    attrNames.toString()));
        }
    }

    //http://conf.aplana.com/pages/viewpage.action?pageId=11402881
    //TODO : Не закончена постановка
    private void createPeriods(Department department){
        //1
        if (department.getType() != DepartmentType.TERR_BANK){

        }
        //2
        List<ReportPeriod> reportPeriodsIncome = periodService.getPeriodsByTaxTypeAndDepartments(TaxType.INCOME, Arrays.asList(0));
        List<ReportPeriod> reportPeriodsVat = periodService.getPeriodsByTaxTypeAndDepartments(TaxType.VAT, Arrays.asList(0));
        if (reportPeriodsIncome != null && reportPeriodsIncome.size() >= 1 || reportPeriodsVat != null && reportPeriodsVat.size() >= 1){

        }
    }

    //Проверка использования
    private void isInUsed(final Department department, Logger logger){

        //TODO : В FormDataSearchService добавить метод поиска записей по фильтру аналогично findDataByUserIdAndFilter, а то на каждый чих новый метод

        //2 точка запроса
        List<DeclarationData> decDataIds = declarationDataSearchService.getDeclarationData(new DeclarationDataFilter(){{setDepartmentIds(Arrays.asList(department.getId()));}},
                DeclarationDataSearchOrdering.ID, true);
        if (!decDataIds.isEmpty()){
            for (DeclarationData decData : decDataIds){
                logger.error(String.format("Существует экземпляр декларации %s в подразделении %s в периоде %s!",
                        decData.getDeclarationTemplateId(),
                        department.getName(),
                        periodService.getReportPeriod(decData.getReportPeriodId()).getName()));
            }
        }

        //4 точка запроса
        List<DepartmentFormType> departmentFormTypes = sourceService.getDFTByDepartment(department.getId(), null);
        if (!departmentFormTypes.isEmpty()){
            for (DepartmentFormType dft : departmentFormTypes){
                FormType formType =  formTypeService.get(dft.getFormTypeId());
                logger.warn(String.format("Существует назначение формы %s типа %s подразделению %s!",
                        formType.getName(), dft.getKind().getName(), department.getName())
                );
            }
        }

        //5 точка запроса
        List<DepartmentDeclarationType> departmentDeclarationTypes = sourceService.getDDTByDepartment(department.getId(), null);
        if (!departmentDeclarationTypes.isEmpty()){
            for (DepartmentDeclarationType ddt : departmentDeclarationTypes){
                DeclarationType declarationType = declarationTypeService.get(ddt.getDeclarationTypeId());
                logger.warn(String.format("Существует назначение декларации %s подразделению %s!",
                        declarationType.getName(), department.getName()));
            }
        }

        //7 точка запроса
        List<TAUserFull> users = taUserService.getByFilter(new MembersFilterData(){{
            setDepartmentIds(new HashSet<Integer>(Arrays.asList(department.getId())));
        }});
        if (!users.isEmpty()){
            for (TAUserFull taUserFull : users)
                logger.error(String.format("Подразделению %s назначен пользовател с логином %s!", department.getName(), taUserFull.getUser().getName()));
        }
    }
}
