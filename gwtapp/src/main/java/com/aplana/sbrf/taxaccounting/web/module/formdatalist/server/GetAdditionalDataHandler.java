package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetAdditionalData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetAdditionalDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        result.setMonthly(formTemplateService.isMonthly(formTemplateId));
        result.setComparative(formTemplateService.isComparative(formTemplateId));

        if (result.isMonthly()) {
            //Заполняем доступные месяцы
            result.setMonthsList(periodService.getAvailableMonthList(action.getReportPeriodId()));
        }
        if (result.isComparative()) {
            //Заполняем доступные периоды сравнения
            result.setComparativPeriods(periodService.getComparativPeriods(action.getTaxType(), action.getDepartmentId()));
        }
        return result;
    }

    @Override
    public void undo(GetAdditionalData monthlyState, GetAdditionalDataResult monthlyStateResult, ExecutionContext executionContext) throws ActionException {
    }
}
