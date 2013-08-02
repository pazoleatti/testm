package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Получение параметров подразделения и списка доступных налоговых периодов
 *
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDepartmentCombinedHandler extends AbstractActionHandler<GetDepartmentCombinedAction,
        GetDepartmentCombinedResult> {

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Autowired
    private ReportPeriodService reportService;

    @Autowired
    private RefBookFactory rbFactory;

    public GetDepartmentCombinedHandler() {
        super(GetDepartmentCombinedAction.class);
    }

    @Override
    public GetDepartmentCombinedResult execute(GetDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {

        DepartmentCombined depCombined = new DepartmentCombined();

        // Параметры пагинации
        PagingParams pp = new PagingParams();
        pp.setCount(1);
        pp.setStartIndex(0);

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

        String filter = DepartmentParamAliases.DEPARTMENT_ID.name() + " = " + action.getDepartmentId();
        PagingResult<Map<String, RefBookValue>> params = provider.getRecords(
                calendarFrom.getTime(), pp, filter, null);

        if (params.getRecords().size() != 0) {
            Map<String, RefBookValue> paramsMap = params.getRecords().get(0);
            // Общая часть
            depCombined.setDepartmentId(paramsMap.get(DepartmentParamAliases.DEPARTMENT_ID.name()).getReferenceValue());
            depCombined.setDictRegionId(paramsMap.get(DepartmentParamAliases.DICT_REGION_ID.name()).getReferenceValue());
            depCombined.setOkato(paramsMap.get(DepartmentParamAliases.OKATO.name()).getReferenceValue());
            depCombined.setInn(paramsMap.get(DepartmentParamAliases.INN.name()).getStringValue());
            depCombined.setKpp(paramsMap.get(DepartmentParamAliases.KPP.name()).getStringValue());
            depCombined.setTaxOrganCode(paramsMap.get(DepartmentParamAliases.TAX_ORGAN_CODE.name()).getStringValue());
            depCombined.setOkvedCode(paramsMap.get(DepartmentParamAliases.OKVED_CODE.name()).getReferenceValue());
            depCombined.setPhone(paramsMap.get(DepartmentParamAliases.PHONE.name()).getStringValue());
            depCombined.setReorgFormCode(paramsMap.get(DepartmentParamAliases.REORG_FORM_CODE.name()).getReferenceValue());
            depCombined.setReorgInn(paramsMap.get(DepartmentParamAliases.REORG_INN.name()).getStringValue());
            depCombined.setReorgKpp(paramsMap.get(DepartmentParamAliases.REORG_KPP.name()).getStringValue());
            depCombined.setName(paramsMap.get(DepartmentParamAliases.NAME.name()).getStringValue());

            // Общая частная часть
            depCombined.setSignatoryId(paramsMap.get(DepartmentParamAliases.SIGNATORY_ID.name()).getReferenceValue());
            depCombined.setSignatorySurname(paramsMap.get(DepartmentParamAliases.SIGNATORY_SURNAME.name()).getStringValue());
            depCombined.setSignatoryFirstname(paramsMap.get(DepartmentParamAliases.SIGNATORY_FIRSTNAME.name()).getStringValue());
            depCombined.setSignatoryLastname(paramsMap.get(DepartmentParamAliases.SIGNATORY_LASTNAME.name()).getStringValue());
            depCombined.setApproveDocName(paramsMap.get(DepartmentParamAliases.APPROVE_DOC_NAME.name()).getStringValue());
            depCombined.setApproveOrgName(paramsMap.get(DepartmentParamAliases.APPROVE_ORG_NAME.name()).getStringValue());
            depCombined.setTaxPlaceTypeCode(paramsMap.get(DepartmentParamAliases.TAX_PLACE_TYPE_CODE.name()).getReferenceValue());
            depCombined.setAppVersion(paramsMap.get(DepartmentParamAliases.APP_VERSION.name()).getStringValue());
            depCombined.setFormatVersion(paramsMap.get(DepartmentParamAliases.FORMAT_VERSION.name()).getStringValue());

            // УКС и налог на прибыль
            if (action.getTaxType() == TaxType.INCOME || action.getTaxType() == TaxType.DEAL) {
                Number sumTax = paramsMap.get(DepartmentParamAliases.SUM_TAX.name()).getNumberValue();
                depCombined.setSumTax(sumTax == null ? null : sumTax.longValue());
                Number sumDividends = paramsMap.get(DepartmentParamAliases.SUM_DIVIDENDS.name()).getNumberValue();
                depCombined.setSumDividends(sumDividends == null ? null : sumDividends.longValue() );

                // Налог на прибыль
                if (action.getTaxType() == TaxType.INCOME) {
                    depCombined.setObligation(paramsMap.get(DepartmentParamAliases.TAX_PLACE_TYPE_CODE.name()).getReferenceValue());
                    depCombined.setTaxRate((Double) paramsMap.get(DepartmentParamAliases.FORMAT_VERSION.name()).getNumberValue());
                    depCombined.setType(paramsMap.get(DepartmentParamAliases.TYPE.name()).getReferenceValue());
                }
            }
        }

        GetDepartmentCombinedResult result = new GetDepartmentCombinedResult();
        result.setDepartmentCombined(depCombined);

        return result;
    }

    @Override
    public void undo(GetDepartmentCombinedAction action, GetDepartmentCombinedResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
