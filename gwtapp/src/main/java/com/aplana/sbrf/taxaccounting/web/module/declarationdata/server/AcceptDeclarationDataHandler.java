package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.AcceptDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class AcceptDeclarationDataHandler extends AbstractActionHandler<AcceptDeclarationDataAction, AcceptDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;


	@Autowired
	private SecurityService securityService;

    public AcceptDeclarationDataHandler() {
        super(AcceptDeclarationDataAction.class);
    }

    @Override
    public AcceptDeclarationDataResult execute(AcceptDeclarationDataAction action, ExecutionContext context) {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		declarationDataService.setAccepted(action.getDeclarationId(), action.isAccepted(), userId);
	    return new AcceptDeclarationDataResult();
    }

    @Override
    public void undo(AcceptDeclarationDataAction action, AcceptDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
