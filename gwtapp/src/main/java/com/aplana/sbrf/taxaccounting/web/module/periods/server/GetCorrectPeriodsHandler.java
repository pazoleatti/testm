package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetCorrectPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetCorrectPeriodsResult;
import com.google.gwt.rpc.server.RPC;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_UNP')")
public class GetCorrectPeriodsHandler extends AbstractActionHandler<GetCorrectPeriodsAction, GetCorrectPeriodsResult> {

    public GetCorrectPeriodsHandler() {
        super(GetCorrectPeriodsAction.class);
    }

    @Autowired
    private PeriodService periodService;

    @Override
    public GetCorrectPeriodsResult execute(GetCorrectPeriodsAction action, ExecutionContext executionContext) throws ActionException {
        GetCorrectPeriodsResult result = new GetCorrectPeriodsResult();
        PagingParams params = new PagingParams();
        params.setProperty("id");
        params.setDirection("ASC");
        PagingResult<ReportPeriod> periods = periodService.getCorrectPeriods(action.getTaxType(), action.getDepartmentId(), params);
        result.setReportPeriod(periods);
        return result;
    }

    @Override
    public void undo(GetCorrectPeriodsAction getCorrectPeriodsAction, GetCorrectPeriodsResult getCorrectPeriodsResult, ExecutionContext executionContext) throws ActionException {

    }
}
