package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedAction;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.GetDepartmentCombinedResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
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
    private PeriodService reportService;

    @Autowired
    private RefBookFactory rbFactory;

    public GetDepartmentCombinedHandler() {
        super(GetDepartmentCombinedAction.class);
    }

    @Override
    public GetDepartmentCombinedResult execute(GetDepartmentCombinedAction action, ExecutionContext executionContext)
            throws ActionException {

        DepartmentCombined depCombined = new DepartmentCombined();
        depCombined.setTaxType(action.getTaxType());

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

        if (params.size() != 0) {
            Map<String, RefBookValue> paramsMap = params.get(0);
             if (params.size() != 1) {
                 throw new ActionException("Miltiple RefBook records (version = "+
                         new SimpleDateFormat("dd.MM.yyyy").format(calendarFrom.getTime()));
             }

            // Id записи
            depCombined.setRecordId(paramsMap.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue());

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

            // Налог на прибыль
            if (action.getTaxType() == TaxType.INCOME) {
                Number sumTax = paramsMap.get(DepartmentParamAliases.SUM_TAX.name()).getNumberValue();
                depCombined.setSumTax(sumTax == null ? null : sumTax.longValue());
                Number sumDividends = paramsMap.get(DepartmentParamAliases.SUM_DIVIDENDS.name()).getNumberValue();
                depCombined.setSumDividends(sumDividends == null ? null : sumDividends.longValue());
                depCombined.setObligation(paramsMap.get(DepartmentParamAliases.OBLIGATION.name()).getReferenceValue());
                Number taxRate = paramsMap.get(DepartmentParamAliases.TAX_RATE.name()).getNumberValue();
                depCombined.setTaxRate(taxRate == null ? null : taxRate.doubleValue());
                depCombined.setType(paramsMap.get(DepartmentParamAliases.TYPE.name()).getReferenceValue());
            }
        }

        GetDepartmentCombinedResult result = new GetDepartmentCombinedResult();
        result.setDepartmentCombined(depCombined);

        // Если запись не нашлась, то готовим новую
        if (result.getDepartmentCombined().getDepartmentId() == null && action.getDepartmentId() != null) {
            result.getDepartmentCombined().setDepartmentId(action.getDepartmentId().longValue());
        }

        // Признак открытости
        result.setReportPeriodActive(reportService.isActivePeriod(action.getReportPeriodId(), action.getDepartmentId()));

        // Получение текстовых значений справочника
        Map<Long, String> rbTextValues = new HashMap<Long, String>();

        if (depCombined.getDictRegionId() != null) {
            rbTextValues.put(9L, getStringValue(rbFactory.getDataProvider(4L).getValue(depCombined.getDictRegionId(), 9L)));
        }
        if (depCombined.getOkato() != null) {
            rbTextValues.put(7L, getStringValue(rbFactory.getDataProvider(3L).getValue(depCombined.getOkato(), 7L)));
        }
        if (depCombined.getOkvedCode() != null) {
            rbTextValues.put(210L, getStringValue(rbFactory.getDataProvider(34L).getValue(depCombined.getOkvedCode(), 210L)));
        }
        if (depCombined.getReorgFormCode() != null) {
            rbTextValues.put(13L, getStringValue(rbFactory.getDataProvider(5L).getValue(depCombined.getReorgFormCode(), 13L)));
        }
        if (depCombined.getSignatoryId() != null) {
            rbTextValues.put(213L, getStringValue(rbFactory.getDataProvider(35L).getValue(depCombined.getSignatoryId(), 213L)));
        }
        if (depCombined.getTaxPlaceTypeCode() != null) {
            rbTextValues.put(3L, getStringValue(rbFactory.getDataProvider(2L).getValue(depCombined.getTaxPlaceTypeCode(), 3L)));
        }
        if (depCombined.getObligation() != null) {
            rbTextValues.put(110L, getNumberValue(rbFactory.getDataProvider(25L).getValue(depCombined.getObligation(), 110L)));
        }
        if (depCombined.getType() != null) {
            rbTextValues.put(120L, getNumberValue(rbFactory.getDataProvider(26L).getValue(depCombined.getType(), 120L)));
        }

        result.setRbTextValues(rbTextValues);

        return result;
    }

    private String getStringValue(RefBookValue value) {
        if (value == null) {
            return null;
        }
        return value.getStringValue();
    }

    private String getNumberValue(RefBookValue value) {
        if (value == null) {
            return null;
        }
        if (value.getNumberValue() == null) {
            return null;
        }
        return value.getNumberValue().toString();
    }

    @Override
    public void undo(GetDepartmentCombinedAction action, GetDepartmentCombinedResult result,
                     ExecutionContext executionContext) throws ActionException {
        // Не требуется
    }
}
