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
import java.util.*;

/**
 * Получение параметров подразделения и списка доступных налоговых периодов
 *
 * @author Dmitriy Levykin
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
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
            depCombined.setDepartmentId(getList(paramsMap.get(DepartmentParamAliases.DEPARTMENT_ID.name()).getReferenceValue()));
            depCombined.setDictRegionId(getList(paramsMap.get(DepartmentParamAliases.DICT_REGION_ID.name()).getReferenceValue()));
            depCombined.setOkato(getList(paramsMap.get(DepartmentParamAliases.OKATO.name()).getReferenceValue()));
            depCombined.setInn(paramsMap.get(DepartmentParamAliases.INN.name()).getStringValue());
            depCombined.setKpp(paramsMap.get(DepartmentParamAliases.KPP.name()).getStringValue());
            depCombined.setTaxOrganCode(paramsMap.get(DepartmentParamAliases.TAX_ORGAN_CODE.name()).getStringValue());
            depCombined.setOkvedCode(getList(paramsMap.get(DepartmentParamAliases.OKVED_CODE.name()).getReferenceValue()));
            depCombined.setPhone(paramsMap.get(DepartmentParamAliases.PHONE.name()).getStringValue());
            depCombined.setReorgFormCode(getList(paramsMap.get(DepartmentParamAliases.REORG_FORM_CODE.name()).getReferenceValue()));
            depCombined.setReorgInn(paramsMap.get(DepartmentParamAliases.REORG_INN.name()).getStringValue());
            depCombined.setReorgKpp(paramsMap.get(DepartmentParamAliases.REORG_KPP.name()).getStringValue());
            depCombined.setName(paramsMap.get(DepartmentParamAliases.NAME.name()).getStringValue());

            // Общая частная часть
            depCombined.setSignatoryId(getList(paramsMap.get(DepartmentParamAliases.SIGNATORY_ID.name()).getReferenceValue()));
            depCombined.setSignatorySurname(paramsMap.get(DepartmentParamAliases.SIGNATORY_SURNAME.name()).getStringValue());
            depCombined.setSignatoryFirstname(paramsMap.get(DepartmentParamAliases.SIGNATORY_FIRSTNAME.name()).getStringValue());
            depCombined.setSignatoryLastname(paramsMap.get(DepartmentParamAliases.SIGNATORY_LASTNAME.name()).getStringValue());
            depCombined.setApproveDocName(paramsMap.get(DepartmentParamAliases.APPROVE_DOC_NAME.name()).getStringValue());
            depCombined.setApproveOrgName(paramsMap.get(DepartmentParamAliases.APPROVE_ORG_NAME.name()).getStringValue());
            depCombined.setTaxPlaceTypeCode(getList(paramsMap.get(DepartmentParamAliases.TAX_PLACE_TYPE_CODE.name()).getReferenceValue()));
            depCombined.setAppVersion(paramsMap.get(DepartmentParamAliases.APP_VERSION.name()).getStringValue());
            depCombined.setFormatVersion(paramsMap.get(DepartmentParamAliases.FORMAT_VERSION.name()).getStringValue());

            // Налог на прибыль
            if (action.getTaxType() == TaxType.INCOME) {
                Number sumTax = paramsMap.get(DepartmentParamAliases.SUM_TAX.name()).getNumberValue();
                depCombined.setSumTax(sumTax == null ? null : sumTax.longValue());
                Number sumDividends = paramsMap.get(DepartmentParamAliases.SUM_DIVIDENDS.name()).getNumberValue();
                depCombined.setSumDividends(sumDividends == null ? null : sumDividends.longValue());
                depCombined.setObligation(getList(paramsMap.get(DepartmentParamAliases.OBLIGATION.name()).getReferenceValue()));
                Number taxRate = paramsMap.get(DepartmentParamAliases.TAX_RATE.name()).getNumberValue();
                depCombined.setTaxRate(taxRate == null ? null : taxRate.doubleValue());
                depCombined.setType(getList(paramsMap.get(DepartmentParamAliases.TYPE.name()).getReferenceValue()));
            }
        }

        GetDepartmentCombinedResult result = new GetDepartmentCombinedResult();
        result.setDepartmentCombined(depCombined);

        // Если запись не нашлась, то готовим новую
        if (result.getDepartmentCombined().getDepartmentId() == null && action.getDepartmentId() != null) {
            result.getDepartmentCombined().setDepartmentId(getList(action.getDepartmentId().longValue()));
        }

        // Признак открытости
        result.setReportPeriodActive(reportService.isActivePeriod(action.getReportPeriodId(), action.getDepartmentId()));

        // Получение текстовых значений справочника
        Map<Long, String> rbTextValues = new HashMap<Long, String>();

        if (depCombined.getDictRegionId() != null && !depCombined.getDictRegionId().isEmpty()) {
            rbTextValues.put(9L, getStringValue(rbFactory.getDataProvider(4L).getValue(depCombined.getDictRegionId().get(0), 9L)));
        }
        if (depCombined.getOkato() != null && !depCombined.getOkato().isEmpty()) {
            rbTextValues.put(7L, getStringValue(rbFactory.getDataProvider(3L).getValue(depCombined.getOkato().get(0), 7L)));
        }
        if (depCombined.getOkvedCode() != null && !depCombined.getOkvedCode().isEmpty()) {
            rbTextValues.put(210L, getStringValue(rbFactory.getDataProvider(34L).getValue(depCombined.getOkvedCode().get(0), 210L)));
        }
        if (depCombined.getReorgFormCode() != null && !depCombined.getReorgFormCode().isEmpty()) {
            rbTextValues.put(13L, getStringValue(rbFactory.getDataProvider(5L).getValue(depCombined.getReorgFormCode().get(0), 13L)));
        }
        if (depCombined.getSignatoryId() != null && !depCombined.getSignatoryId().isEmpty()) {
            rbTextValues.put(213L, getStringValue(rbFactory.getDataProvider(35L).getValue(depCombined.getSignatoryId().get(0), 213L)));
        }
        if (depCombined.getTaxPlaceTypeCode() != null && !depCombined.getTaxPlaceTypeCode().isEmpty()) {
            rbTextValues.put(3L, getStringValue(rbFactory.getDataProvider(2L).getValue(depCombined.getTaxPlaceTypeCode().get(0), 3L)));
        }
        if (depCombined.getObligation() != null && !depCombined.getObligation().isEmpty()) {
            rbTextValues.put(110L, getNumberValue(rbFactory.getDataProvider(25L).getValue(depCombined.getObligation().get(0), 110L)));
        }
        if (depCombined.getType() != null && !depCombined.getType().isEmpty()) {
            rbTextValues.put(120L, getNumberValue(rbFactory.getDataProvider(26L).getValue(depCombined.getType().get(0), 120L)));
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

    private List<Long> getList(Long value) {
        List<Long> list = null;
        if (value != null) {
            list = new ArrayList<Long>();
            list.add(value);
        }
        return list;
    }
}
