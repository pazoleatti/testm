package com.aplana.sbrf.taxaccounting.web.module.admin.server;

import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetScriptAction;
import com.aplana.sbrf.taxaccounting.web.module.admin.shared.GetScriptResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

/**
 * @author Vitalii Samolovskikh
 */
public class GetScriptHandler extends AbstractActionHandler<GetScriptAction, GetScriptResult> {
    public GetScriptHandler() {
        super(GetScriptAction.class);
    }

    @Override
    public GetScriptResult execute(GetScriptAction action, ExecutionContext context) throws ActionException {
        // TODO
        return null;
    }

    @Override
    public void undo(GetScriptAction action, GetScriptResult result, ExecutionContext context) throws ActionException {
        // Nothing!!!
    }
}
