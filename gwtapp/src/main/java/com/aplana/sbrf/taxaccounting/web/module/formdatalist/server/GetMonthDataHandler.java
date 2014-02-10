package com.aplana.sbrf.taxaccounting.web.module.formdatalist.server;

import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetMonthData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetMonthDataResult;
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
public class GetMonthDataHandler extends AbstractActionHandler<GetMonthData, GetMonthDataResult> {

    @Autowired
    private FormTemplateService formTemplateService;

    @Autowired
    private PeriodService periodService;

    public GetMonthDataHandler() {
        super(GetMonthData.class);
    }

    @Override
    public GetMonthDataResult execute(GetMonthData action, ExecutionContext executionContext) throws ActionException {
        GetMonthDataResult result = new GetMonthDataResult();
        result.setMonthly(formTemplateService.isMonthly(action.getTypeId()));
        result.setMonthsList(periodService.getMonthList(action.getPeriodId()));
        return result;
    }

    @Override
    public void undo(GetMonthData monthlyState, GetMonthDataResult monthlyStateResult, ExecutionContext executionContext) throws ActionException {
    }
}
