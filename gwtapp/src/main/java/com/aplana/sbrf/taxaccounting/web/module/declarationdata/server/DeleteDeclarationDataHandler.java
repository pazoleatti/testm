package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

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
@PreAuthorize("hasAnyRole('N_ROLE_OPER', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_OPER', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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
		declarationDataService.delete(action.getDeclarationId(), securityService.currentUserInfo());
	    return new DeleteDeclarationDataResult();
    }

    @Override
    public void undo(DeleteDeclarationDataAction action, DeleteDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
