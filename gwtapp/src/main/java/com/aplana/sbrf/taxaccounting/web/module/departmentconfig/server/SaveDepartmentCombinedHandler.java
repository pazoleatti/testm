package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.SaveDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class SaveDepartmentCombinedHandler extends AbstractActionHandler<SaveDepartmentCombinedAction,
        SaveDepartmentCombinedResult> {

    @Autowired
    private PeriodService reportService;

    @Autowired
    private RefBookFactory rbFactory;

    @Autowired
    private LogEntryService logEntryService;

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
                && action.getTaxType() != null
                && action.getReportPeriodId() != null) {

            RefBookDataProvider provider = null;

            switch (action.getTaxType()) {
                case INCOME:
                    provider = rbFactory.getDataProvider(33L);
                    break;
                case TRANSPORT:
                    provider = rbFactory.getDataProvider(31L);
                    break;
                case DEAL:
                    provider = rbFactory.getDataProvider(37L);
                    break;
            }

            Calendar calendarFrom = reportService.getStartDate(action.getReportPeriodId());

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
            paramsMap.put(DepartmentParamAliases.REORG_FORM_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, getFirstLong(depCombined.getReorgFormCode())));
            paramsMap.put(DepartmentParamAliases.REORG_INN.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getReorgInn()));
            paramsMap.put(DepartmentParamAliases.REORG_KPP.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getReorgKpp()));
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
            if (depCombined.getRecordId() == null) {
                provider.createRecordVersion(logger, calendarFrom.getTime(), null, Arrays.asList(record));
            } else {
                provider.updateRecordVersion(logger, depCombined.getRecordId(), calendarFrom.getTime(), null, paramsMap);
            }

            // Запись ошибок в лог при наличии
            if (!logger.getEntries().isEmpty()) {
                result.setUuid(logEntryService.save(logger.getEntries()));
                if (logger.containsLevel(LogLevel.ERROR)) {
                    result.setHasError(true);
                }
            }
        }
        return result;
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
