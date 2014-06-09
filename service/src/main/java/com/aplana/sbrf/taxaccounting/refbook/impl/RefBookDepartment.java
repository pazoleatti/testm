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
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.DepartmentType.TERR_BANK;
import static com.aplana.sbrf.taxaccounting.model.DepartmentType.fromCode;

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
    private static final String DEPARTMENT_TABLE_NAME = "DEPARTMENT";
    private static final String DEPARTMENT_TYPE_ATTRIBUTE = "TYPE";
    private static final String DEPARTMENT_PARENT_ATTRIBUTE = "PARENT_ID";
    private static final String WARN_MESSAGE_TARGET =
            "Внимание! Форма %s подразделения %s при сохранении будет являться приемником для формы %s подразделения %s, относящимся к разным территориальным банкам";
    private static final String WARN_MESSAGE_SOURCE =
            "Внимание! Форма %s подразделения %s при сохранении будет являться источником для формы %s подразделения %s, относящимся к разным территориальным банкам";


    public static final String DESTINATION_FTS = "destinationFTs";
    public static final String SOURCE_FTS = "sourceFTs";
    public static final String DESTINATION_DTS = "destinationDTs";
    public static final String SOURCE_DTS = "sourceDTs";

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
    AuditService auditService;
    @Autowired
    private RefBookFactory rbFactory;


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
    public int getRecordsCount(Date version, String filter) {
        return refBookDepartmentDao.getRecordsCount(filter);
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
        List<RefBookAttribute> attributes = refBookDao.getAttributes(REF_BOOK_ID);
        Map<String, RefBookValue> refBookValueMap = records.get(0).getValues();
        checkCorrectness(logger, attributes, records);
        if (logger.containsLevel(LogLevel.ERROR))
            return new ArrayList<Long>(0);
        int depId = refBookDepartmentDao.create(refBookValueMap, attributes);
        int terrBankId = departmentService.getParentTB(depId).getId();
        createPeriods(depId, fromCode(refBookValueMap.get(DEPARTMENT_TYPE_ATTRIBUTE).getNumberValue().intValue()),
                terrBankId, logger);

        return Arrays.asList((long)depId);
    }

    @Override
    public List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long recordId) {
        return new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
    }

    //http://conf.aplana.com/pages/viewpage.action?pageId=11378355
    @SuppressWarnings("unchecked")
    @Override
    public void updateRecordVersion(Logger logger, final Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        final Department dep = departmentService.getDepartment(uniqueRecordId.intValue());
        DepartmentType oldType = dep.getType();
        DepartmentType newType = fromCode(records.get(DEPARTMENT_TYPE_ATTRIBUTE).getNumberValue().intValue());
        boolean isChangeType = oldType != newType;
        boolean isHasOpenPeriods = false;

        int oldTBId = departmentService.getParentTB(uniqueRecordId.intValue()).getId();
        int newTBId =
                records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue() != null && records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue().intValue() != 0?
                departmentService.getParentTB(records.get(DEPARTMENT_PARENT_ATTRIBUTE).getReferenceValue().intValue()).getId()
                : 0;
        boolean isChangeTB = oldTBId != newTBId;

        if (isChangeType){
            switch (oldType){
                //3 шаг
                case ROOT_BANK :
                    logger.error("Подразделению не может быть изменен тип \"Банк\"!\"");
                    return;
                //4 шаг
                case TERR_BANK:
                    List<ReportPeriod> openReportPeriods =
                            new ArrayList<ReportPeriod>(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.TRANSPORT, Arrays.asList(uniqueRecordId.intValue()), true, true));
                    openReportPeriods.addAll(periodService.getOpenPeriodsByTaxTypeAndDepartments(TaxType.PROPERTY, Arrays.asList(uniqueRecordId.intValue()), true, true));
                    isHasOpenPeriods = !openReportPeriods.isEmpty();
                    if (isHasOpenPeriods){
                        for (ReportPeriod period : openReportPeriods)
                            logger.warn(
                                    "Для подразделения %s для налога %s открыт период %s",
                                    dep.getName(),
                                    period.getTaxPeriod().getTaxType().getName(),
                                    period.getName());
                        throw new ServiceLoggerException("Подразделению не может быть изменен тип \"ТБ\", если для него существует период!",
                                logEntryService.save(logger.getEntries()));
                    }
                    break;
                //5 шаг
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
        //6 шаг
        if (isHasOpenPeriods){
            checkCorrectness(logger, attributes, Arrays.asList(refBookRecord));
            if (logger.containsLevel(LogLevel.ERROR))
                throw new ServiceLoggerException("Подразделение не сохранено, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
        }

        //7
        if (versionFrom != null){
            if (oldType != TERR_BANK){
                //7А.3.1.1А  (7А.3.1.1А.1 - 7А.3.1.1А.5)
                if (isChangeTB){
                    formDataService.updateFDTBNames(newTBId, oldTBId, versionFrom, versionTo);
                }
                //7А.3.1.2
                formDataService.updateFDDepartmentNames(dep.getId(), records.get(DEPARTMENT_TYPE_ATTRIBUTE).getStringValue(), versionFrom, versionTo);
            }
        }

        //10 шаг
        if (isChangeTB){
            //8A.1
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
                //10A 1.1, 10А.1.1А

                Map<String, List> sourcesDestinations = sourceService.getSourcesDestinations(uniqueRecordId.intValue(), newTBId, Arrays.asList(TaxType.TRANSPORT, TaxType.PROPERTY));
                List<Pair<DepartmentFormType, DepartmentFormType>> destinationFTs = sourcesDestinations.get(DESTINATION_FTS);
                List<Pair<DepartmentFormType, DepartmentFormType>> sourceFTs = sourcesDestinations.get(SOURCE_FTS);
                List<Pair<DepartmentFormType, DepartmentDeclarationType>> destinationDTs = sourcesDestinations.get(DESTINATION_DTS);
                List<Pair<DepartmentFormType, DepartmentDeclarationType>> sourceDTs = sourcesDestinations.get(SOURCE_DTS);
                for (Pair<DepartmentFormType, DepartmentFormType> destinationDFTPair : destinationFTs){
                    logger.warn(WARN_MESSAGE_TARGET,
                            formTypeService.get(destinationDFTPair.getSecond().getFormTypeId()).getName(),
                            departmentService.getDepartment(destinationDFTPair.getSecond().getDepartmentId()).getName(),
                            formTypeService.get(destinationDFTPair.getFirst().getFormTypeId()).getName(),
                            departmentService.getDepartment(newTBId).getName());
                }
                for (Pair<DepartmentFormType, DepartmentFormType> sourceDFTPair : sourceFTs){
                    logger.warn(WARN_MESSAGE_SOURCE,
                            formTypeService.get(sourceDFTPair.getFirst().getFormTypeId()).getName(),
                            departmentService.getDepartment(sourceDFTPair.getFirst().getDepartmentId()).getName(),
                            formTypeService.get(sourceDFTPair.getSecond().getFormTypeId()).getName(),
                            departmentService.getDepartment(newTBId).getName());
                }
                for (Pair<DepartmentFormType, DepartmentDeclarationType> destinationDDTPair : destinationDTs){
                    logger.warn(WARN_MESSAGE_TARGET,
                            declarationTypeService.get(destinationDDTPair.getSecond().getDeclarationTypeId()).getName(),
                            departmentService.getDepartment(destinationDDTPair.getSecond().getDepartmentId()).getName(),
                            formTypeService.get(destinationDDTPair.getFirst().getFormTypeId()).getName(),
                            departmentService.getDepartment(newTBId).getName());
                }
                for (Pair<DepartmentFormType, DepartmentDeclarationType> sourceDDTPair : sourceDTs){
                    logger.warn(WARN_MESSAGE_SOURCE,
                            formTypeService.get(sourceDDTPair.getFirst().getFormTypeId()).getName(),
                            departmentService.getDepartment(sourceDDTPair.getFirst().getDepartmentId()).getName(),
                            declarationTypeService.get(sourceDDTPair.getSecond().getDeclarationTypeId()).getName(),
                            departmentService.getDepartment(newTBId).getName());
                }
                createPeriods(uniqueRecordId, newType, newTBId, logger);
            }
        }

        //Сохранение
        refBookDepartmentDao.update(uniqueRecordId, records, refBook.getAttributes());
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        int depId = uniqueRecordIds.get(0).intValue();
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

        Collection<Long> dftIsd = CollectionUtils.collect(sourceService.getDFTByDepartment(depId, null),
                new Transformer() {
                    @Override
                    public Object transform(Object o) {
                        return ((DepartmentFormType) o).getId();
                    }
                });
        if (!dftIsd.isEmpty())
            sourceService.deleteDFT(dftIsd);
        Collection<Long> ddtIds = CollectionUtils.collect(sourceService.getDDTByDepartment(depId, null),
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

        deletePeriods(depId, logger);

        refBookDepartmentDao.remove(depId);
        /*auditService.add(FormDataEvent.LOGOUT, null, 0, null, null, null, null,
                "");*/
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

    private void checkCorrectness(Logger logger, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        if (departmentService.getBankDepartment().getType().getCode() ==
                records.get(0).getValues().get(DEPARTMENT_TYPE_ATTRIBUTE).getNumberValue().intValue()){
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
        List<Pair<Long,String>> matchedRecords = refBookDepartmentDao.getMatchedRecordsByUniqueAttributes(REF_BOOK_ID, attributes, records);
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
    private void createPeriods(long depId, DepartmentType newDepartmentType, int terrBankId, Logger logger){
        //1
        if (newDepartmentType != DepartmentType.TERR_BANK){
            if (departmentService.getParentTB((int) depId) != null){
                //1А.1.1
                List<Long> reportPeriods =
                        refBookDepartmentDao.getPeriodsByTaxTypesAndDepartments(
                                Arrays.asList(TaxType.values()),
                                Arrays.asList(terrBankId));
                if (!reportPeriods.isEmpty()){
                    for (Long periodIs : reportPeriods)
                        //1А.1.1.1
                        if (periodService.existForDepartment((int) depId, periodIs))
                            return;
                    //1А.1.1.1А
                    for (Long periodIs : reportPeriods){
                        DepartmentReportPeriod drp = new DepartmentReportPeriod();
                        drp.setReportPeriod(periodService.getReportPeriod(periodIs.intValue()));
                        drp.setDepartmentId(depId);
                        drp.setActive(true);
                        drp.setCorrectPeriod(null);
                        periodService.saveOrUpdate(drp, null, logger.getEntries());
                    }
                    return;
                }
                return;
            }
        }
        //2
        List<Long> reportPeriods =
                refBookDepartmentDao.getPeriodsByTaxTypesAndDepartments(Arrays.asList(TaxType.INCOME, TaxType.DEAL, TaxType.VAT), Arrays.asList(0));
        if (!reportPeriods.isEmpty()){
            for (Long periodIs : reportPeriods){
                DepartmentReportPeriod drp = new DepartmentReportPeriod();
                drp.setReportPeriod(periodService.getReportPeriod(periodIs.intValue()));
                drp.setDepartmentId(depId);
                drp.setActive(periodService.isPeriodOpen(0, periodIs));
                drp.setCorrectPeriod(null);
                periodService.saveOrUpdate(
                        drp,
                        null,
                        logger.getEntries());
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
        List<DepartmentFormType> departmentFormTypes = sourceService.getDFTByDepartment(department.getId(), null);
        for (DepartmentFormType dft : departmentFormTypes){
            FormType formType =  formTypeService.get(dft.getFormTypeId());
            logger.warn(String.format("Существует назначение формы %s типа %s подразделению %s!",
                    formType.getName(), dft.getKind().getName(), department.getName())
            );
        }

        //5 точка запроса
        List<DepartmentDeclarationType> departmentDeclarationTypes = sourceService.getDDTByDepartment(department.getId(), null);
        for (DepartmentDeclarationType ddt : departmentDeclarationTypes){
            DeclarationType declarationType = declarationTypeService.get(ddt.getDeclarationTypeId());
            logger.warn(String.format("Существует назначение декларации %s подразделению %s!",
                    declarationType.getName(), department.getName()));
        }

        //6 точка запроса
        List<Long> ref101 = refBookIncome101.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, department.getId()));
        List<Long> ref102 = refBookIncome102.getUniqueRecordIds(null, String.format(FILTER_BY_DEPARTMENT, department.getId()));
        for (Long id : ref101){
            Map<String, RefBookValue> values = refBookIncome101.getRecordData(id);
            logger.warn(String.format("Существует загруженная для подразделения %s бух. отчетность в периоде %s!",
                    department.getName(), periodService.getReportPeriod(values.get("REPORT_PERIOD_ID").getNumberValue().intValue())));
        }
        for (Long id : ref102){
            Map<String, RefBookValue> values = refBookIncome102.getRecordData(id);
            logger.warn(String.format("Существует загруженная для подразделения %s бух. отчетность в периоде %s!",
                    department.getName(), periodService.getReportPeriod(values.get("REPORT_PERIOD_ID").getNumberValue().intValue())));
        }

        //7 точка запроса
        List<TAUserFull> users = taUserService.getByFilter(new MembersFilterData(){{
            setDepartmentIds(new HashSet<Integer>(Arrays.asList(department.getId())));
        }});
        for (TAUserFull taUserFull : users)
            logger.error(String.format("Подразделению %s назначен пользовател с логином %s!", department.getName(), taUserFull.getUser().getName()));

        //8 точка запроса
        List<DepartmentFormType> departmentFormTypesDest = sourceService.getFormDestinations(department.getId(), 0, null);
        List<DepartmentDeclarationType> departmentDeclarationTypesDest = sourceService.getDeclarationDestinations(department.getId(), 0, null);
        List<DepartmentFormType> depFTSources = sourceService.getDFTSourcesByDFT(department.getId(), 0 , null);
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
                    declarationTypeService.get(departmentDeclarationType.getDeclarationTypeId())));
        }
        for (DepartmentFormType departmentFormType : depFTSources){
            logger.warn(String.format("назначение является приёмником для %s - %s - %s приемника",
                    department.getName(),
                    departmentFormType.getKind().getName(),
                    formTypeService.get(departmentFormType.getFormTypeId()).getName()));
        }
    }

    private void deletePeriods(int depId, Logger logger){
        List<Long> reportPeriods =
                refBookDepartmentDao.getPeriodsByTaxTypesAndDepartments(
                        Arrays.asList(TaxType.values()),
                        Arrays.asList(depId));
        for (Long id : reportPeriods){
            periodService.removePeriodWithLog(id.intValue(), null, Arrays.asList(depId), null, logger.getEntries());
        }

    }
}
