package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateEventScript;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.AddScriptAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.AddScriptResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole({'N_ROLE_CONF'})")
public class AddScriptHandler extends AbstractActionHandler<AddScriptAction, AddScriptResult>{

    @Autowired
    DeclarationTemplateService declarationTemplateService;

    public AddScriptHandler() {
        super(AddScriptAction.class);
    }

    @Override
    public AddScriptResult execute(AddScriptAction action, ExecutionContext context) throws ActionException {
        AddScriptResult result = new AddScriptResult();
        if (declarationTemplateService.checkIfEventScriptPresent(action.getDeclarationTemplateId(), action.getFormDataEventId())) {
            result.setDeclarationTemplateEventScript(null);
        } else {
            DeclarationTemplateEventScript declarationTemplateEventScript = new DeclarationTemplateEventScript();
            declarationTemplateEventScript.setDeclarationTemplateId(action.getDeclarationTemplateId());
            declarationTemplateEventScript.setEventId(action.getFormDataEventId());
            declarationTemplateEventScript.setScript("println script");
            result.setDeclarationTemplateEventScript(declarationTemplateEventScript);
        }
        return result;
    }

    @Override
    public void undo(AddScriptAction action, AddScriptResult result, ExecutionContext context) throws ActionException {

    }
}
