package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AddDeclarationSourceAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.AddDeclarationSourceResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class AddDeclarationSourceHandler extends AbstractActionHandler<AddDeclarationSourceAction, AddDeclarationSourceResult> {{
}

	public AddDeclarationSourceHandler() {
		super(AddDeclarationSourceAction.class);
	}

	@Autowired
	SourceService departmentFormTypeService;

	@Override
	public AddDeclarationSourceResult execute(AddDeclarationSourceAction action, ExecutionContext executionContext) throws ActionException {
		for (Integer depId : action.getDepartmentId()) {
			for (Integer dt : action.getDeclarationTypeId()) {
				boolean canAssign = true;
				for (DepartmentDeclarationType ddt : departmentFormTypeService.getDDTByDepartment(depId.intValue(), action.getTaxType())) {
					if (ddt.getDeclarationTypeId() == dt) {
						canAssign = false;
					}
				}
				if (canAssign) {
					departmentFormTypeService.saveDDT((long)depId, dt);
				}
			}
		}
		return new AddDeclarationSourceResult();
	}

	@Override
	public void undo(AddDeclarationSourceAction addDeclarationSourceAction, AddDeclarationSourceResult addDeclarationSourceResult, ExecutionContext executionContext) throws ActionException {
	}
}
