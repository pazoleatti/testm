package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasCorrectionPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckHasCorrectionPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class CheckHasCorrectionPeriodHandler extends AbstractActionHandler<CheckHasCorrectionPeriodAction, CheckHasCorrectionPeriodResult> {

    @Autowired
    private PeriodService periodService;

    public CheckHasCorrectionPeriodHandler() {
        super(CheckHasCorrectionPeriodAction.class);
    }

    @Override
    public CheckHasCorrectionPeriodResult execute(CheckHasCorrectionPeriodAction action, ExecutionContext executionContext) throws ActionException {
        CheckHasCorrectionPeriodResult result = new CheckHasCorrectionPeriodResult();
        List<DepartmentReportPeriod> drp = periodService.listByDepartmentIdAndTaxType((long)action.getDepartmentId(), action.getTaxType());
        result.setHasCorrectionPeriods(false);
        for (DepartmentReportPeriod rp : drp) {
            if ((rp.getReportPeriod().getId() == action.getReportPeriodId())
                    &&(rp.getCorrectPeriod() != null)) {
                result.setHasCorrectionPeriods(true);
            }
        }
        return result;
    }

    @Override
    public void undo(CheckHasCorrectionPeriodAction checkHasCorrectionPeriodAction, CheckHasCorrectionPeriodResult checkHasCorrectionPeriodResult, ExecutionContext executionContext) throws ActionException {

    }
}
