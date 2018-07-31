package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetChecksAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetChecksResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;


@Component
@PreAuthorize("hasAnyRole('N_ROLE_CONF')")
public class GetChecksHandler extends AbstractActionHandler<GetChecksAction, GetChecksResult> {

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	public GetChecksHandler() {
		super(GetChecksAction.class);
	}

	@Override
	public GetChecksResult execute(GetChecksAction action, ExecutionContext context) throws ActionException {
		GetChecksResult result = new GetChecksResult();
		result.setChecks(declarationTemplateService.getChecks(action.getDeclarationTypeId(), action.getDeclarationTemplateId()));
        return result;
    }

	@Override
	public void undo(GetChecksAction arg0, GetChecksResult arg1, ExecutionContext arg2) throws ActionException {
		// Ничего не делаем
	}
}
