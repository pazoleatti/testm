package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.CheckDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class CheckDeclarationDataHandler extends AbstractActionHandler<CheckDeclarationDataAction, CheckDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    public CheckDeclarationDataHandler() {
        super(CheckDeclarationDataAction.class);
    }

    @Override
    public CheckDeclarationDataResult execute(CheckDeclarationDataAction action, ExecutionContext context) {
		CheckDeclarationDataResult result = new CheckDeclarationDataResult();
        Logger logger = new Logger();
		declarationDataService.check(logger, action.getDeclarationId(), securityService.currentUserInfo());
        result.setUuid(logEntryService.save(logger.getEntries()));
	    return result;
    }

    @Override
    public void undo(CheckDeclarationDataAction action, CheckDeclarationDataResult result, ExecutionContext context)
			throws ActionException {
        // Nothing!
    }
}
