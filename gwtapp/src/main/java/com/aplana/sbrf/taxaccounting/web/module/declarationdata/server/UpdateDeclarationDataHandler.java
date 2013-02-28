package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.UpdateDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class UpdateDeclarationDataHandler extends AbstractActionHandler<UpdateDeclarationDataAction, UpdateDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    public UpdateDeclarationDataHandler() {
        super(UpdateDeclarationDataAction.class);
    }

    @Override
    public UpdateDeclarationDataResult execute(UpdateDeclarationDataAction action, ExecutionContext context) {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		if(action.isDelete()) {
			declarationDataService.delete(action.getDeclarationData().getId(), userId);
		}
		else if(action.isRefresh()) {
			declarationDataService.refreshDeclaration(new Logger(), action.getDeclarationData().getId(), userId);
		} else {
			declarationDataService.setAccepted(action.getDeclarationData().getId(), action.getDeclarationData().isAccepted(), userId);
		}
	    return new UpdateDeclarationDataResult();
    }

    @Override
    public void undo(UpdateDeclarationDataAction action, UpdateDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
