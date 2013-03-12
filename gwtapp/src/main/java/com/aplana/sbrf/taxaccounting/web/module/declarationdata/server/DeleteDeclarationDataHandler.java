package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.DeleteDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.DeleteDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class DeleteDeclarationDataHandler extends AbstractActionHandler<DeleteDeclarationDataAction, DeleteDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    public DeleteDeclarationDataHandler() {
        super(DeleteDeclarationDataAction.class);
    }

    @Override
    public DeleteDeclarationDataResult execute(DeleteDeclarationDataAction action, ExecutionContext context) {
		TAUser user = securityService.currentUser();
		Integer userId = user.getId();
		declarationDataService.delete(action.getDeclarationId(), userId);
	    return new DeleteDeclarationDataResult();
    }

    @Override
    public void undo(DeleteDeclarationDataAction action, DeleteDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }

}
