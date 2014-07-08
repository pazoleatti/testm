package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.RefreshDeclaration;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.RefreshDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class RefreshDeclarationHandler extends AbstractActionHandler<RefreshDeclaration, RefreshDeclarationResult> {

	public RefreshDeclarationHandler() {
		super(RefreshDeclaration.class);
	}

	@Autowired
	DeclarationDataService declarationDataService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

	@Override
	public RefreshDeclarationResult execute(RefreshDeclaration command, ExecutionContext executionContext) throws ActionException {
		Logger  logger = new Logger();
		declarationDataService.calculate(logger, command.getDeclarationDataId(), securityService.currentUserInfo(), new Date());
		RefreshDeclarationResult result = new RefreshDeclarationResult();
		if (logger.containsLevel(LogLevel.ERROR)) {
			result.setSuccess(false);
		} else {
			result.setSuccess(true);
		}
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(RefreshDeclaration createDeclaration, RefreshDeclarationResult createDeclarationResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
