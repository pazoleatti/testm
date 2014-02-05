package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CreateNewDTTypeAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.CreateNewDTTypeResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * User: avanteev
 */
@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class CreateNewDTTypeHandler extends AbstractActionHandler<CreateNewDTTypeAction, CreateNewDTTypeResult> {

    public CreateNewDTTypeHandler() {
        super(CreateNewDTTypeAction.class);
    }

    @Override
    public CreateNewDTTypeResult execute(CreateNewDTTypeAction action, ExecutionContext context) throws ActionException {
        return new CreateNewDTTypeResult();
    }

    @Override
    public void undo(CreateNewDTTypeAction action, CreateNewDTTypeResult result, ExecutionContext context) throws ActionException {

    }
}
