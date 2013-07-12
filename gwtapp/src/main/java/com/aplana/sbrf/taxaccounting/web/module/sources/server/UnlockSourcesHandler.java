package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UnlockSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.UnlockSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class UnlockSourcesHandler extends AbstractActionHandler<UnlockSourcesAction, UnlockSourcesResult> {

	@Autowired
	private DeclarationTemplateService declarationTemplateService;

	@Autowired
	private SecurityService securityService;

	public UnlockSourcesHandler() {
		super(UnlockSourcesAction.class);
	}

	@Override
	public UnlockSourcesResult execute(UnlockSourcesAction action, ExecutionContext executionContext) throws ActionException {
		UnlockSourcesResult result = new UnlockSourcesResult();
/*		result.setUnlockedSuccessfully(declarationTemplateService.unlock(action.getDeclarationId(),
				securityService.currentUserInfo()));*/

		return result;
	}

	@Override
	public void undo(UnlockSourcesAction unlockFormAction, UnlockSourcesResult unlockFormResult, ExecutionContext executionContext) throws ActionException {
		// Ничего не делаем
	}
}
