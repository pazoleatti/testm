package com.aplana.sbrf.taxaccounting.web.module.audit.server;

import com.aplana.sbrf.taxaccounting.service.script.TaxPeriodService;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetTaxPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetTaxPeriodResult;
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
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class GetTaxPeriodHandler extends AbstractActionHandler<GetTaxPeriodAction, GetTaxPeriodResult> {

    @Autowired
    TaxPeriodService taxPeriodService;

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
