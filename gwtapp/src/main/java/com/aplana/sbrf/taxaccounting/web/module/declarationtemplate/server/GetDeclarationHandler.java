package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class GetDeclarationHandler extends AbstractActionHandler<GetDeclarationAction, GetDeclarationResult> {
    //@Autowired
	//private DeclarationTemplateService declarationTemplateService;

    public GetDeclarationHandler() {
        super(GetDeclarationAction.class);
    }

    @Override
    public GetDeclarationResult execute(GetDeclarationAction action, ExecutionContext context) throws ActionException {
		GetDeclarationResult result = new GetDeclarationResult();
		//DeclarationTemplate declarationTemplate = declarationTemplateService.get(action.getId());
		//result.setDeclarationTemplate(declarationTemplate);
        return result;
    }

    @Override
    public void undo(GetDeclarationAction action, GetDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
