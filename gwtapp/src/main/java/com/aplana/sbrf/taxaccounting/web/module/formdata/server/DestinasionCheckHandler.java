package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DestinationCheckAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.DestinationCheckResult;
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
public class DestinasionCheckHandler extends AbstractActionHandler<DestinationCheckAction, DestinationCheckResult> {

    @Autowired
    private FormDataService formDataService;

    public DestinasionCheckHandler() {
        super(DestinationCheckAction.class);
    }

    @Override
    public DestinationCheckResult execute(DestinationCheckAction action, ExecutionContext context) throws ActionException {
        formDataService.checkDestinations(action.getFormDataId());
        return new DestinationCheckResult();
    }

    @Override
    public void undo(DestinationCheckAction action, DestinationCheckResult result, ExecutionContext context) throws ActionException {
        //Nothing
    }
}
