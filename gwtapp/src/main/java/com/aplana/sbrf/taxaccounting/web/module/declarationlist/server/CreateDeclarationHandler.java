package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateDeclaration;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CreateDeclarationResult;

import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateDeclarationHandler extends AbstractActionHandler<CreateDeclaration, CreateDeclarationResult> {

	public CreateDeclarationHandler() {
		super(CreateDeclaration.class);
	}

	@Autowired
    private DeclarationDataService declarationDataService;
	@Autowired
    private DeclarationTemplateService declarationTemplateService;
	@Autowired
	private SecurityService securityService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;

	@Override
	public CreateDeclarationResult execute(CreateDeclaration command, ExecutionContext executionContext) throws ActionException {
        CreateDeclarationResult result = new CreateDeclarationResult();
        Logger logger = new Logger();

        DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.getLast(command.getDepartmentId(),
                command.getReportPeriodId());
        Integer declarationTypeId = command.getDeclarationTypeId();

        if (departmentReportPeriod == null) {
            throw new ActionException("Не удалось определить налоговый период.");
        }

        int activeDeclarationTemplateId = declarationTemplateService.getActiveDeclarationTemplateId(declarationTypeId,
                departmentReportPeriod.getReportPeriod().getId());
        long declarationId = declarationDataService.create(logger, activeDeclarationTemplateId,
                securityService.currentUserInfo(), departmentReportPeriod, null,
                null, null, null, null, null);
        result.setDeclarationId(declarationId);
        if (!logger.getEntries().isEmpty()){
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
	}

	@Override
	public void undo(CreateDeclaration createDeclaration, CreateDeclarationResult createDeclarationResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
