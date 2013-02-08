package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateDeclarationAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class UpdateDeclarationDataHandler extends AbstractActionHandler<UpdateDeclarationAction, UpdateDeclarationResult> {
	@Autowired
	private DeclarationService declarationService;

	@Autowired
	private SecurityService securityService;

    public UpdateDeclarationDataHandler() {
        super(UpdateDeclarationAction.class);
    }

    @Override
    public UpdateDeclarationResult execute(UpdateDeclarationAction action, ExecutionContext context) {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();

		UpdateDeclarationResult result = new UpdateDeclarationResult();
		declarationService.setAccepted(action.getDeclaration().getId(), action.getDeclaration().isAccepted(), userId);
		return result;
    }

    @Override
    public void undo(UpdateDeclarationAction action, UpdateDeclarationResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
