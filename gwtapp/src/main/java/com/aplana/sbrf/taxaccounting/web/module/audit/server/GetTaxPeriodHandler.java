package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetTaxPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetTaxPeriodResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetTaxPeriodHandler extends AbstractActionHandler<GetTaxPeriodAction, GetTaxPeriodResult> {

    @Autowired
    ReportPeriodService taxPeriodService;

    public GetTaxPeriodHandler() {
        super(GetTaxPeriodAction.class);
    }

    @Override
    public GetTaxPeriodResult execute(GetTaxPeriodAction getTaxPeriodAction, ExecutionContext executionContext) throws ActionException {
        GetTaxPeriodResult result =  new GetTaxPeriodResult();
        result.setTaxPeriods(taxPeriodService.listByTaxType(getTaxPeriodAction.getTaxType()));
        return result;
    }

    @Override
    public void undo(GetTaxPeriodAction getTaxPeriodAction, GetTaxPeriodResult getTaxPeriodResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
