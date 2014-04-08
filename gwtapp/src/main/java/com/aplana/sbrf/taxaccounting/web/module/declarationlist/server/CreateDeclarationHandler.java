package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CreateDeclarationHandler extends AbstractActionHandler<CreateDeclaration, CreateDeclarationResult> {

	public CreateDeclarationHandler() {
		super(CreateDeclaration.class);
	}

	@Autowired
	DeclarationDataService declarationDataService;

	@Autowired
	DeclarationTemplateService declarationTemplateService;

    @Autowired
    DeclarationTypeService declarationTypeService;

	@Autowired
	private SecurityService securityService;

    @Autowired
    private LogEntryService logEntryService;

	@Override
	public CreateDeclarationResult execute(CreateDeclaration command, ExecutionContext executionContext) throws ActionException {
        Integer declarationTypeId = command.getDeclarationTypeId();
        if (command.getTaxType().equals(TaxType.DEAL)) {
            List<DeclarationType> declarationTypeList = declarationTypeService.getTypes(command.getDepartmentId(), command.getReportPeriodId(), TaxType.DEAL);
            if (declarationTypeList.size() == 1) {
                declarationTypeId = declarationTypeList.get(0).getId();
            } else {
                throw new ActionException("Не удалось определить шаблон для уведомления.");
            }
        }
        CreateDeclarationResult result = new CreateDeclarationResult();
        Logger logger = new Logger();
		result.setDeclarationId(declarationDataService.create(logger, declarationTemplateService
				.getActiveDeclarationTemplateId(declarationTypeId, command.getReportPeriodId()), command.getDepartmentId(),
				securityService.currentUserInfo(), command.getReportPeriodId()));
        result.setUuid(logEntryService.save(logger.getEntries()));
		return result;
	}

	@Override
	public void undo(CreateDeclaration createDeclaration, CreateDeclarationResult createDeclarationResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
