package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.UnlockDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.UnlockDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONF', 'F_ROLE_CONF')")
public class UnlockDeclarationHandler extends AbstractActionHandler<UnlockDeclarationAction, UnlockDeclarationResult> {

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private SecurityService securityService;

	public UnlockDeclarationHandler() {
		super(UnlockDeclarationAction.class);
	}

	@Override
	public UnlockDeclarationResult execute(UnlockDeclarationAction action, ExecutionContext executionContext) throws ActionException {
		UnlockDeclarationResult result = new UnlockDeclarationResult();
		result.setUnlockedSuccessfully(declarationTemplateService.unlock(action.getDeclarationId(),
				securityService.currentUserInfo()));

		return result;
	}

	@Override
	public void undo(UnlockDeclarationAction unlockFormAction, UnlockDeclarationResult unlockFormResult, ExecutionContext executionContext) throws ActionException {
		// Ничего не делаем
	}
}
