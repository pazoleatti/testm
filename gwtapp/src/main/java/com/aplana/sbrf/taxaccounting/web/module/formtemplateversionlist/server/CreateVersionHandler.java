package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.server;

import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.CreateVersionAction;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.CreateVersionResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * User: avanteev
 */
public class CreateVersionHandler extends AbstractActionHandler<CreateVersionAction, CreateVersionResult> {
    public CreateVersionHandler() {
        super(CreateVersionAction.class);
    }

    @Override
    public CreateVersionResult execute(CreateVersionAction action, ExecutionContext context) throws ActionException {
        return null;
    }

    @Override
    public void undo(CreateVersionAction action, CreateVersionResult result, ExecutionContext context) throws ActionException {

    }
}
