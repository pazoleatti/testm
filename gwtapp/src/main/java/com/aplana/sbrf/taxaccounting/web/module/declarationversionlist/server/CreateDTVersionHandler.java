package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.server;

import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.CreateDTVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared.CreateDTVersionResult;
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
public class CreateDTVersionHandler extends AbstractActionHandler<CreateDTVersionAction, CreateDTVersionResult> {

    public CreateDTVersionHandler() {
        super(CreateDTVersionAction.class);
    }

    @Override
    public CreateDTVersionResult execute(CreateDTVersionAction action, ExecutionContext context) throws ActionException {
        CreateDTVersionResult result = new CreateDTVersionResult();
        return result;
    }

    @Override
    public void undo(CreateDTVersionAction action, CreateDTVersionResult result, ExecutionContext context) throws ActionException {

    }
}
