package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetAdditionalData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetAdditionalDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;

/**
 * Проверяет является ли налоговая форма ежемесячной и возвращает список доступных месяцев.
 * @author fmukhametdinov
 */
@Service
public class GetAdditionalDataHandler extends AbstractActionHandler<GetAdditionalData, GetAdditionalDataResult> {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private PeriodService periodService;

    public GetAdditionalDataHandler() {
        super(GetAdditionalData.class);
    }

    @Override
    public GetAdditionalDataResult execute(GetAdditionalData action, ExecutionContext executionContext) throws ActionException {

        GetAdditionalDataResult result = new GetAdditionalDataResult();
        Integer formTemplateId = formTemplateService.getActiveFormTemplateId(action.getTypeId(), action.getReportPeriodId());
        FormTemplate formTemplate = formTemplateService.get(formTemplateId);
        result.setMonthly(formTemplate.isMonthly());
        result.setComparative(formTemplate.isComparative());
        result.setAccruing(formTemplate.isAccruing());
        if (formTemplate.isAccruing() && (!formTemplate.isComparative() || formTemplate.isComparative() && action.getComparativeReportPeriodId()  != null)) {
            int reportPeriodId;
            if (formTemplate.isComparative()) {
                reportPeriodId = action.getComparativeReportPeriodId();
            } else {
                reportPeriodId = action.getReportPeriodId();
            }
            result.setFirstPeriod(periodService.isFirstPeriod(reportPeriodId));
        }

        if (result.isMonthly()) {
            //Заполняем доступные месяцы
            result.setMonthsList(periodService.getAvailableMonthList(action.getReportPeriodId()));
        }
        if (result.isComparative()) {
            //Заполняем доступные периоды сравнения
            result.setComparativePeriods(periodService.getComparativPeriods(action.getTaxType(), action.getDepartmentId()));
        }
        return result;
    }

    @Override
    public void undo(GetAdditionalData monthlyState, GetAdditionalDataResult monthlyStateResult, ExecutionContext executionContext) throws ActionException {
    }
}
