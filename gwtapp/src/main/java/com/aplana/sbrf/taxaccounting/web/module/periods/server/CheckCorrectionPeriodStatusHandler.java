package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.PeriodStatusBeforeOpen;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckCorrectionPeriodStatusAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckCorrectionPeriodStatusResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
@Service
public class CheckCorrectionPeriodStatusHandler extends AbstractActionHandler<CheckCorrectionPeriodStatusAction, CheckCorrectionPeriodStatusResult> {

    public CheckCorrectionPeriodStatusHandler() {
        super(CheckCorrectionPeriodStatusAction.class);
    }

    @Autowired
    PeriodService periodService;

    @Override
    public CheckCorrectionPeriodStatusResult execute(CheckCorrectionPeriodStatusAction action, ExecutionContext executionContext) throws ActionException {
        PeriodStatusBeforeOpen status =
                periodService.checkPeriodStatusBeforeOpen(action.getSelectedPeriod(), action.getSelectedDepartments().get(0), action.getTerm());
        CheckCorrectionPeriodStatusResult result = new CheckCorrectionPeriodStatusResult();
        result.setStatus(status);
        return result;
    }

    @Override
    public void undo(CheckCorrectionPeriodStatusAction checkCorrectionPeriodStatusAction, CheckCorrectionPeriodStatusResult checkCorrectionPeriodStatusResult, ExecutionContext executionContext) throws ActionException {

    }
}
