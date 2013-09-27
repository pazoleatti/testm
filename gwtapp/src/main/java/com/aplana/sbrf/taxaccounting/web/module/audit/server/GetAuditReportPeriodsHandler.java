package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetReportPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetReportPeriodsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.PeriodService;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetAuditReportPeriodsHandler extends AbstractActionHandler<GetReportPeriodsAction, GetReportPeriodsResult> {

    @Autowired
    PeriodService periodService;

    public GetAuditReportPeriodsHandler() {
        super(GetReportPeriodsAction.class);
    }

    @Override
    public GetReportPeriodsResult execute(GetReportPeriodsAction getReportPeriodsAction, ExecutionContext executionContext) throws ActionException {
        GetReportPeriodsResult result =  new GetReportPeriodsResult();
        result.setReportPeriods(periodService.getAllPeriodsByTaxType(getReportPeriodsAction.getTaxType(), false));
        return result;
    }

    @Override
    public void undo(GetReportPeriodsAction getReportPeriodsAction, GetReportPeriodsResult getReportPeriodsResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
