package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.UpdateDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.UpdateDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class UpdateDeclarationHandler extends AbstractActionHandler<UpdateDeclarationAction, UpdateDeclarationResult> {
	//@Autowired
	//private DeclarationTemplateService declarationTemplateService;

    public UpdateDeclarationHandler() {
        super(UpdateDeclarationAction.class);
    }

    @Override
    public UpdateDeclarationResult execute(UpdateDeclarationAction action, ExecutionContext context) {
		UpdateDeclarationResult result = new UpdateDeclarationResult();

		//declarationTemplateService.save(action.getDeclarationTemplate());
		return result;
    }

    @Override
    public void undo(UpdateDeclarationAction action, UpdateDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
