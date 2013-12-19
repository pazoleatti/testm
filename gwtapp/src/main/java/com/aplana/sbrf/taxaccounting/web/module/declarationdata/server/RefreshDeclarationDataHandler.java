package com.aplana.sbrf.taxaccounting.web.module.declarationdata.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RefreshDeclarationDataAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RefreshDeclarationDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class RefreshDeclarationDataHandler extends AbstractActionHandler<RefreshDeclarationDataAction, RefreshDeclarationDataResult> {
	@Autowired
	private DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

    public RefreshDeclarationDataHandler() {
        super(RefreshDeclarationDataAction.class);
    }

    @Override
    public RefreshDeclarationDataResult execute(RefreshDeclarationDataAction action, ExecutionContext context) {
		TAUserInfo userInfo = securityService.currentUserInfo();
        RefreshDeclarationDataResult result = new RefreshDeclarationDataResult();
        Logger logger = new Logger();
		declarationDataService.reCreate(logger, action.getDeclarationId(), userInfo, action.getDocDate());
        result.setUuid(logEntryService.save(logger.getEntries()));
	    return result;
    }

    @Override
    public void undo(RefreshDeclarationDataAction action, RefreshDeclarationDataResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
