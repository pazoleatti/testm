package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetCheсksAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetCheсksResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;


@Component
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class GetChecksHandler extends AbstractActionHandler<GetCheсksAction, GetCheсksResult> {

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	public GetChecksHandler() {
		super(GetCheсksAction.class);
	}

	@Override
	public GetCheсksResult execute(GetCheсksAction action, ExecutionContext context) throws ActionException {
		GetCheсksResult result = new GetCheсksResult();
		result.setChecks(declarationTemplateService.getChecks(action.getDeclarationTypeId(), action.getDeclarationTemplateId()));
        return result;
    }

	@Override
	public void undo(GetCheсksAction arg0, GetCheсksResult arg1, ExecutionContext arg2) throws ActionException {
		// Ничего не делаем
	}
}
