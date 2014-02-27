package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetDeclarationPopUpFilterAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.GetDeclarationPopUpFilterResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetDeclarationPopUpFilterHandler extends AbstractActionHandler<GetDeclarationPopUpFilterAction, GetDeclarationPopUpFilterResult> {{
}

	public GetDeclarationPopUpFilterHandler() {
		super(GetDeclarationPopUpFilterAction.class);
	}

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SourceService sourceService;

	@Override
	public GetDeclarationPopUpFilterResult execute(GetDeclarationPopUpFilterAction action, ExecutionContext executionContext) throws ActionException {
		TAUser currUser = securityService.currentUserInfo().getUser();

		GetDeclarationPopUpFilterResult result = new GetDeclarationPopUpFilterResult();

		Set<Integer> availableDepartmentSet = new HashSet<Integer>();
		List<Department> availableDepartmentList = departmentService.getBADepartments(currUser);
		for (Department d: availableDepartmentList){
			availableDepartmentSet.add(d.getId());
		}
		result.setAvailableDepartments(availableDepartmentSet);

		result.setDepartments(availableDepartmentList);

		Set<Integer> availablePerformersSet = new HashSet<Integer>();
		List<Department> availablePerformersList = departmentService.getDestinationDepartments(currUser);
		for (Department d: availablePerformersList){
			availablePerformersSet.add(d.getId());
		}

		result.setDeclarationTypes(sourceService.allDeclarationTypeByTaxType(action.getTaxType()));

		return result;
	}

	@Override
	public void undo(GetDeclarationPopUpFilterAction getDeclarationPopUpFilterAction, GetDeclarationPopUpFilterResult getDeclarationPopUpFilterResult, ExecutionContext executionContext) throws ActionException {

	}
}
