package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.server;

import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessReportPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessReportPeriodsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_OPER', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetHistoryBusinessReportPeriodHandler
        extends AbstractActionHandler<GetHistoryBusinessReportPeriodsAction, GetHistoryBusinessReportPeriodsResult> {

    @Autowired
    PeriodService periodService;

    public GetHistoryBusinessReportPeriodHandler() {
        super(GetHistoryBusinessReportPeriodsAction.class);
    }

    @Override
    public GetHistoryBusinessReportPeriodsResult execute(GetHistoryBusinessReportPeriodsAction action, ExecutionContext context) throws ActionException {
        GetHistoryBusinessReportPeriodsResult result = new GetHistoryBusinessReportPeriodsResult();
        result.setReportPeriodList(periodService.getAllPeriodsByTaxType(action.getTaxType(), false));
        return result;
    }

    @Override
    public void undo(GetHistoryBusinessReportPeriodsAction action, GetHistoryBusinessReportPeriodsResult result, ExecutionContext context) throws ActionException {
        //Not implemented
    }
}
