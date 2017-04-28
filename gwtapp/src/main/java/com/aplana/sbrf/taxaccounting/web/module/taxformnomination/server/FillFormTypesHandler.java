package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.FillFormTypesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.FillFormTypesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author vpetrov
 */

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class FillFormTypesHandler extends AbstractActionHandler<FillFormTypesAction, FillFormTypesResult> {

    @Autowired
    private SecurityService securityService;

    public FillFormTypesHandler() {
        super(FillFormTypesAction.class);
    }

    @Override
    public FillFormTypesResult execute(FillFormTypesAction action, ExecutionContext executionContext) throws ActionException {
        FillFormTypesResult result = new FillFormTypesResult();
        result.setTaxType(action.getTaxType());
        return result;
    }

    @Override
    public void undo(FillFormTypesAction fillFormTypesAction, FillFormTypesResult fillFormTypesResult, ExecutionContext executionContext) throws ActionException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
