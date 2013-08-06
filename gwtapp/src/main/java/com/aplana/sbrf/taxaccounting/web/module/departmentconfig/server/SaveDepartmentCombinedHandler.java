package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
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
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class SaveDepartmentCombinedHandler extends AbstractActionHandler<SaveDepartmentCombinedAction,
        SaveDepartmentCombinedResult> {

    @Autowired
    private ReportPeriodService reportService;

    @Autowired
    private RefBookFactory rbFactory;

    public SaveDepartmentCombinedHandler() {
        super(SaveDepartmentCombinedAction.class);
    }

    @Override
    public SaveDepartmentCombinedResult execute(SaveDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {

        DepartmentCombined depCombined = action.getDepartmentCombined();

        if (depCombined != null
                && depCombined.getDepartmentId() != null
                && depCombined.getTaxType() != null
                && action.getPeriod() != null
                && action.getPeriod().isActive()) {

            RefBookDataProvider provider = null;

            switch (depCombined.getTaxType()) {
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

            Calendar calendarFrom = reportService.getStartDate(action.getPeriod().getId());

            Map<String, RefBookValue> paramsMap = new HashMap<String, RefBookValue>();
            // Id записи
            paramsMap.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getRecordId()));

            // Общая часть
            paramsMap.put(DepartmentParamAliases.DEPARTMENT_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getDepartmentId()));
            paramsMap.put(DepartmentParamAliases.DICT_REGION_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getDictRegionId()));
            paramsMap.put(DepartmentParamAliases.OKATO.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getOkato()));
            paramsMap.put(DepartmentParamAliases.INN.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getInn()));
            paramsMap.put(DepartmentParamAliases.KPP.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getKpp()));
            paramsMap.put(DepartmentParamAliases.TAX_ORGAN_CODE.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getTaxOrganCode()));
            paramsMap.put(DepartmentParamAliases.OKVED_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getOkvedCode()));
            paramsMap.put(DepartmentParamAliases.PHONE.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getPhone()));
            paramsMap.put(DepartmentParamAliases.REORG_FORM_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getReorgFormCode()));
            paramsMap.put(DepartmentParamAliases.REORG_INN.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getReorgInn()));
            paramsMap.put(DepartmentParamAliases.REORG_KPP.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getReorgKpp()));
            paramsMap.put(DepartmentParamAliases.NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getName()));

            // Общая частная часть
            paramsMap.put(DepartmentParamAliases.SIGNATORY_ID.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getSignatoryId()));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_SURNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatorySurname()));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_FIRSTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatoryFirstname()));
            paramsMap.put(DepartmentParamAliases.SIGNATORY_LASTNAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getSignatoryLastname()));
            paramsMap.put(DepartmentParamAliases.APPROVE_DOC_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getApproveDocName()));
            paramsMap.put(DepartmentParamAliases.APPROVE_ORG_NAME.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getApproveOrgName()));
            paramsMap.put(DepartmentParamAliases.TAX_PLACE_TYPE_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getTaxPlaceTypeCode()));
            paramsMap.put(DepartmentParamAliases.APP_VERSION.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getAppVersion()));
            paramsMap.put(DepartmentParamAliases.FORMAT_VERSION.name(), new RefBookValue(RefBookAttributeType.STRING, depCombined.getFormatVersion()));

            // УКС и налог на прибыль
            if (depCombined.getTaxType() == TaxType.INCOME || depCombined.getTaxType() == TaxType.DEAL) {
                paramsMap.put(DepartmentParamAliases.SUM_TAX.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getSumTax()));
                paramsMap.put(DepartmentParamAliases.SUM_DIVIDENDS.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getSumDividends()));

                // Налог на прибыль
                if (depCombined.getTaxType() == TaxType.INCOME) {
                    paramsMap.put(DepartmentParamAliases.TAX_PLACE_TYPE_CODE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getObligation()));
                    paramsMap.put(DepartmentParamAliases.FORMAT_VERSION.name(), new RefBookValue(RefBookAttributeType.NUMBER, depCombined.getTaxRate()));
                    paramsMap.put(DepartmentParamAliases.TYPE.name(), new RefBookValue(RefBookAttributeType.REFERENCE, depCombined.getType()));
                }
            }
            provider.updateRecords(calendarFrom.getTime(), Arrays.asList(paramsMap));
        }

        return new SaveDepartmentCombinedResult();
    }

    @Override
    public void undo(SaveDepartmentCombinedAction action, SaveDepartmentCombinedResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
