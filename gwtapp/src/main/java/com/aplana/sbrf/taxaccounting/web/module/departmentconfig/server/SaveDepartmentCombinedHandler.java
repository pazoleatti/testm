package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Arrays.asList;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SaveDepartmentCombinedHandler extends AbstractActionHandler<SaveDepartmentCombinedAction,
        SaveDepartmentCombinedResult> {

    private static final String FORM_WARN = "В налоговой форме \"%s\" в подразделении \"%s\" периоде \"%s\" используется старая версия настроек.";
    private static final String FORM_WARN_S = "В форме \"%s\" в подразделении \"%s\" периоде \"%s\" используется старая версия настроек.";

    private static final String DECLARATION_WARN = "В декларации \"%s\" в подразделении \"%s\" периоде \"%s\" используется старая версия настроек.";
    private static final String DECLARATION_WARN_D = "В уведомлении \"%s\" в подразделении \"%s\" периоде \"%s\" используется старая версия настроек.";

    private static final String SUCCESS_INFO = "Настройки для \"%s\" в периоде с %s по %s успешно сохранены.";

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    private TAUserService userService;

    @Autowired
    private PeriodService reportService;

    @Autowired
    private RefBookFactory rbFactory;

    @Autowired
    private DeclarationDataSearchService declarationDataSearchService;

    @Autowired
    private FormDataSearchService formDataSearchService;

    @Autowired
    private FormDataService formDataService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    DataRowService dataRowService;

    @Autowired
    DepartmentService departmentService;

    public SaveDepartmentCombinedHandler() {
        super(SaveDepartmentCombinedAction.class);
    }

    @Override
    public SaveDepartmentCombinedResult execute(SaveDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {
        SaveDepartmentCombinedResult result = new SaveDepartmentCombinedResult();

        DepartmentCombined depCombined = action.getDepartmentCombined();

        if (depCombined != null
                && depCombined.getDepartmentId() != null
                && !depCombined.getDepartmentId().isEmpty()
                && action.getTaxType() != null
                && action.getReportPeriodId() != null) {
            if (!reportService.isActivePeriod(action.getReportPeriodId(), depCombined.getDepartmentId().get(0))) {
                throw new ActionException("Выбранный отчетный период закрыт!");
            }

            Long refBookId = null;
            switch (action.getTaxType()) {
                case INCOME:
                    refBookId = RefBook.DEPARTMENT_CONFIG_INCOME;
                    break;
                case TRANSPORT:
                    refBookId = RefBook.DEPARTMENT_CONFIG_TRANSPORT;
                    break;
                case DEAL:
                    refBookId = RefBook.DEPARTMENT_CONFIG_DEAL;
                    break;
                case VAT:
                    refBookId = RefBook.DEPARTMENT_CONFIG_VAT;
                    break;
                case PROPERTY:
                    refBookId = RefBook.DEPARTMENT_CONFIG_PROPERTY;
                    break;
            }
            RefBookDataProvider provider = rbFactory.getDataProvider(refBookId);

            ReportPeriod period = reportService.getReportPeriod(action.getReportPeriodId());

            Map<String, RefBookValue> paramsMap = new HashMap<String, RefBookValue>();
            // Id записи
            paramsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getRecordId()));

            // Общая часть
            paramsMap.put(DepartmentParamAliases.DEPARTMENT_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getDepartmentId())));
            paramsMap.put(DepartmentParamAliases.DICT_REGION_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getDictRegionId())));
            paramsMap.put(DepartmentParamAliases.OKTMO.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getOktmo())));
            paramsMap.put(DepartmentParamAliases.INN.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getInn()));
            paramsMap.put(DepartmentParamAliases.KPP.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getKpp()));
            paramsMap.put(DepartmentParamAliases.TAX_ORGAN_CODE.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getTaxOrganCode()));
            paramsMap.put(DepartmentParamAliases.OKVED_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getOkvedCode())));
            paramsMap.put(DepartmentParamAliases.PHONE.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getPhone()));

            if (action.getTaxType() != TaxType.VAT) {
                paramsMap.put(DepartmentParamAliases.REORG_FORM_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getReorgFormCode())));
                paramsMap.put(DepartmentParamAliases.REORG_INN.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getReorgInn()));
                paramsMap.put(DepartmentParamAliases.REORG_KPP.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getReorgKpp()));
            }
            paramsMap.put(DepartmentParamAliases.NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getName()));

            // Общая частная часть
            paramsMap.put(DepartmentParamAliases.SIGNATORY_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getSignatoryId())));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_SURNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatorySurname()));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_FIRSTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatoryFirstname()));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_LASTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatoryLastname()));
            paramsMap.put(DepartmentParamAliases.APPROVE_DOC_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getApproveDocName()));
            paramsMap.put(DepartmentParamAliases.APPROVE_ORG_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getApproveOrgName()));
            paramsMap.put(DepartmentParamAliases.TAX_PLACE_TYPE_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getTaxPlaceTypeCode())));
            paramsMap.put(DepartmentParamAliases.APP_VERSION.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getAppVersion()));
            paramsMap.put(DepartmentParamAliases.FORMAT_VERSION.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getFormatVersion()));

            // Налог на прибыль
            if (action.getTaxType() == TaxType.INCOME) {
                paramsMap.put(DepartmentParamAliases.SUM_TAX.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getSumTax()));
                paramsMap.put(DepartmentParamAliases.SUM_DIVIDENDS.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getSumDividends()));
                paramsMap.put(DepartmentParamAliases.OBLIGATION.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getObligation())));
                paramsMap.put(DepartmentParamAliases.TAX_RATE.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getTaxRate()));
                paramsMap.put(DepartmentParamAliases.TYPE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getType())));
            }

            // Транспортный налог
            if (action.getTaxType() == TaxType.TRANSPORT) {
                paramsMap.put(DepartmentParamAliases.PREPAYMENT.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getPrepayment() ? 1L : 0L));
            }

            Logger logger = new Logger();
            RefBookRecord record = new RefBookRecord();
            record.setValues(paramsMap);
            record.setRecordId(depCombined.getRecordId());

            // Проверка необходимости редактирования
            boolean needEdit = false;

            // Поиск версий настроек для указанного подразделения. Если они есть - создаем новую версию с существующим record_id, иначе создаем новый record_id (по сути элемент справочника)
            String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartmentCombined().getDepartmentId();
            List<Pair<Long, Long>> recordPairs = provider.checkRecordExistence(null, filter);
            if (recordPairs.size() != 0) {
                //Проверяем, к одному ли элементу относятся версии
                Set<Long> recordIdSet = new HashSet<Long>();
                for (Pair<Long, Long> pair : recordPairs) {
                    recordIdSet.add(pair.getSecond());
                }

                if (recordIdSet.size() > 1) {
                    throw new ActionException("Версии настроек, отобраные по фильтру, относятся к разным подразделениям");
                }

                // Существуют версии настроек для указанного подразделения
                record.setRecordId(recordPairs.get(0).getSecond());
            }

            // Поиск версий настроек для указанного подразделения. Если они есть - создаем новую версию с существующим record_id, иначе создаем новый record_id (по сути элемент справочника)
            recordPairs = provider.checkRecordExistence(period.getCalendarStartDate(), filter);
            if (recordPairs.size() != 0) {
                needEdit = true;
                // Запись нашлась
                if (recordPairs.size() != 1) {
                    throw new ActionException("Найдено несколько настроек для подразделения ");
                }
                depCombined.setRecordId(recordPairs.get(0).getFirst());
            }

            if (!needEdit) {
                provider.createRecordVersion(logger, period.getCalendarStartDate(), addDayToDate(period.getEndDate(), -1), Arrays.asList(record));
            } else {
                provider.updateRecordVersion(logger, depCombined.getRecordId(), period.getCalendarStartDate(), addDayToDate(period.getEndDate(), -1), paramsMap);
            }

            String periodName = period.getName() + " " + period.getTaxPeriod().getYear();
            String departmentName = departmentService.getDepartment(action.getDepartment()).getName();


            DeclarationDataFilter declarationDataFilter = new DeclarationDataFilter();
            declarationDataFilter.setReportPeriodIds(asList(action.getReportPeriodId()));
            declarationDataFilter.setDepartmentIds(asList(action.getDepartment()));
            declarationDataFilter.setSearchOrdering(DeclarationDataSearchOrdering.DECLARATION_TYPE_NAME);
            declarationDataFilter.setStartIndex(0);
            declarationDataFilter.setCountOfRecords(10);
            declarationDataFilter.setTaxType(action.getTaxType());
            PagingResult<DeclarationDataSearchResultItem> page = declarationDataSearchService.search(declarationDataFilter);
            for(DeclarationDataSearchResultItem item: page) {
                logger.warn(String.format(action.getTaxType().equals(TaxType.DEAL) ? DECLARATION_WARN_D : DECLARATION_WARN, item.getDeclarationType(), departmentName, periodName));
                result.setDeclarationFormFound(true);
            }

            FormDataFilter formDataFilter = new FormDataFilter();
            formDataFilter.setReportPeriodIds(asList(action.getReportPeriodId()));
            ArrayList<Long> formTypeIds = new ArrayList<Long>();
            formTypeIds.add(372L); // приложение 5
            formTypeIds.add(500L); // сводная 5
            formDataFilter.setFormTypeId(formTypeIds);
            formDataFilter.setFormState(WorkflowState.ACCEPTED);
            formDataFilter.setTaxType(action.getTaxType());
            TAUserInfo userInfo = new TAUserInfo();
            userInfo.setIp("127.0.0.1");
            userInfo.setUser(userService.getUser(TAUser.SYSTEM_USER_ID));
            boolean manual = true;
            List<Long> formDataIds = formDataSearchService.findDataIdsByUserAndFilter(userInfo, formDataFilter);
            for(Long formDataId : formDataIds) {
                FormData formData = formDataService.getFormData(userInfo, formDataId, manual, logger);
                PagingResult<DataRow<Cell>> resultDataRow = dataRowService.getDataRows(userInfo, formDataId, null, true, manual);
                for(DataRow<Cell> dataRow : resultDataRow) {
                    BigDecimal regionBankDivisionId = dataRow.getCell("regionBankDivision").getNumericValue();
                    if (regionBankDivisionId != null && regionBankDivisionId.intValue() == action.getDepartment()) {
                        logger.warn(String.format(action.getTaxType().equals(TaxType.DEAL) ? FORM_WARN_S : FORM_WARN, formData.getFormType().getName(), departmentName, periodName));
                        result.setDeclarationFormFound(true);
                        break;
                    }
                }
            }

            if (!logger.containsLevel(LogLevel.ERROR)) {
                String strEndDate = sdf.format(period.getEndDate());  //TODO поменять когда изменять действие "сохранить", связано с задачей http://jira.aplana.com/browse/SBRFACCTAX-6994
                logger.info(String.format(SUCCESS_INFO, departmentName, sdf.format(period.getCalendarStartDate()), strEndDate));
            }
            result.setUuid(logEntryService.save(logger.getEntries()));
            if (logger.containsLevel(LogLevel.ERROR)) {
                result.setHasError(true);
            }
        }
        return result;
    }

    private Date addDayToDate(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    @Override
    public void undo(SaveDepartmentCombinedAction action, SaveDepartmentCombinedResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }

    private Long getFirstLong(List<Long> list) {
        return (list != null && !list.isEmpty() ? list.get(0) : null);
    }
}
