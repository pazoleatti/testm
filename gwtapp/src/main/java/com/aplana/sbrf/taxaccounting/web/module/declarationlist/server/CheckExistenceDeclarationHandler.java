package com.aplana.sbrf.taxaccounting.web.module.declarationlist.server;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTypeService;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckExistenceDeclaration;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.CheckExistenceDeclarationResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class CheckExistenceDeclarationHandler extends AbstractActionHandler<CheckExistenceDeclaration, CheckExistenceDeclarationResult> {

	public CheckExistenceDeclarationHandler() {
		super(CheckExistenceDeclaration.class);
	}

	@Autowired
	DeclarationDataService declarationService;

    @Autowired
    DeclarationTypeService declarationTypeService;

	@Override
	public CheckExistenceDeclarationResult execute(CheckExistenceDeclaration command, ExecutionContext executionContext) throws ActionException {
        Integer declarationTypeId = command.getDeclarationTypeId();
        if (command.getTaxType().equals(TaxType.DEAL)) {
            List<DeclarationType> declarationTypeList = declarationTypeService.getTypes(command.getDepartmentId(), command.getReportPeriodId(), TaxType.DEAL);
            if (declarationTypeList.size() == 1) {
                declarationTypeId = declarationTypeList.get(0).getId();
            } else {
                throw new ActionException("Не удалось определить шаблон для уведомления.");
            }
        }
        DeclarationData declarationData = declarationService.find(declarationTypeId, command.getDepartmentId(), command.getReportPeriodId());
		CheckExistenceDeclarationResult result = new CheckExistenceDeclarationResult();
		if ((declarationData != null)) {
			if (declarationData.isAccepted()) {
				result.setStatus(CheckExistenceDeclarationResult.DeclarationStatus.EXIST_ACCEPTED);
			} else {
				result.setStatus(CheckExistenceDeclarationResult.DeclarationStatus.EXIST_CREATED);
			}
			result.setDeclarationDataId(declarationData.getId());
		} else {
			result.setStatus(CheckExistenceDeclarationResult.DeclarationStatus.NOT_EXIST);
			result.setDeclarationDataId(null);
		}

		return result;
	}

	@Override
	public void undo(CheckExistenceDeclaration createDeclaration, CheckExistenceDeclarationResult createDeclarationResult, ExecutionContext executionContext) throws ActionException {
		//Nothing
	}
}
