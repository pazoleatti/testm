package com.aplana.sbrf.taxaccounting.web.module.refbookdata.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookScriptAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SaveRefBookScriptResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Fail Mukhametdinov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONF')")
public class SaveRefBookScriptHandler extends AbstractActionHandler<SaveRefBookScriptAction, SaveRefBookScriptResult> {

    @Autowired
    private RefBookScriptingService refBookScriptingService;

    public SaveRefBookScriptHandler() {
        super(SaveRefBookScriptAction.class);
    }

    @Override
    public SaveRefBookScriptResult execute(SaveRefBookScriptAction action, ExecutionContext context) throws ActionException {
        refBookScriptingService.saveScript(action.getRefBookId(), action.getScript(), new Logger());
        return null;
    }

    @Override
    public void undo(SaveRefBookScriptAction action, SaveRefBookScriptResult result, ExecutionContext context) throws ActionException {
    }
}