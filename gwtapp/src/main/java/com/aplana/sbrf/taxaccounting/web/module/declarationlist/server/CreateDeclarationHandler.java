package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class CreateDeclarationHandler extends AbstractActionHandler<CreateDeclaration, CreateDeclarationResult> {

	public CreateDeclarationHandler() {
		super(CreateDeclaration.class);
	}

	@Autowired
	DeclarationDataService declarationDataService;

	@Autowired
	DeclarationTemplateService declarationTemplateService;

	@Autowired
	private SecurityService securityService;

	@Override
	public CreateDeclarationResult execute(CreateDeclaration command, ExecutionContext executionContext) throws ActionException {
		CreateDeclarationResult result = new CreateDeclarationResult();
        Logger logger = new Logger();
		result.setDeclarationId(declarationDataService.create(logger, declarationTemplateService
				.getActiveDeclarationTemplateId(command.getDeclarationTypeId()), command.getDepartmentId(),
				securityService.currentUserInfo(), command.getReportPeriodId(), command.getPagesCount()));
        result.setLogEntries(logger.getEntries());
		return result;
	}

	@Override
	public void undo(CreateDeclaration createDeclaration, CreateDeclarationResult createDeclarationResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
