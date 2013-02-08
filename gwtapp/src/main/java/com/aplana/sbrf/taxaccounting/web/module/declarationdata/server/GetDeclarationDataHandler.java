package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.GetDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDeclarationDataHandler extends AbstractActionHandler<GetDeclarationAction, GetDeclarationResult> {
    @Autowired
	private DeclarationService declarationService;

	@Autowired
	private SecurityService securityService;

    public GetDeclarationDataHandler() {
        super(GetDeclarationAction.class);
    }

    @Override
    public GetDeclarationResult execute(GetDeclarationAction action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		GetDeclarationResult result = new GetDeclarationResult();
		//declarationService.checkLockedByAnotherUser(action.getId(), userId);
		Declaration declaration = declarationService.get(action.getId(), userId);
		//declarationTemplateService.lock(action.getId(), userId);
		result.setDeclaration(declaration);
		return result;
    }

    @Override
    public void undo(GetDeclarationAction action, GetDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
