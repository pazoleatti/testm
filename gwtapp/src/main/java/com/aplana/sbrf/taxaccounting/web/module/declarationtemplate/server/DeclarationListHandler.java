package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ROLE_CONF')")
public class DeclarationListHandler	extends AbstractActionHandler<DeclarationListAction, DeclarationListResult> {
	//@Autowired
	//private DeclarationTemplateService declarationTemplateService;

	public DeclarationListHandler() {
		super(DeclarationListAction.class);
	}

	@Override
	public DeclarationListResult execute(DeclarationListAction action, ExecutionContext executionContext) throws ActionException {
		DeclarationListResult result = new DeclarationListResult();
		//result.setDeclarations(declarationTemplateService.listAll());
		return result;
	}

	@Override
	public void undo(DeclarationListAction formListAction, DeclarationListResult formListResult, ExecutionContext executionContext) throws ActionException {
		// Nothing!!!
	}
}

