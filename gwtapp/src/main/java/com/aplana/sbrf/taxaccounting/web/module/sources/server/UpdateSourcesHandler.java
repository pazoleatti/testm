package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UpdateSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class UpdateSourcesHandler extends AbstractActionHandler<UpdateSourcesAction, UpdateSourcesResult> {

    public UpdateSourcesHandler() {
        super(UpdateSourcesAction.class);
    }

    @Override
    public UpdateSourcesResult execute(UpdateSourcesAction action, ExecutionContext context) {
		UpdateSourcesResult result = new UpdateSourcesResult();
		return result;
    }

    @Override
    public void undo(UpdateSourcesAction action, UpdateSourcesResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
