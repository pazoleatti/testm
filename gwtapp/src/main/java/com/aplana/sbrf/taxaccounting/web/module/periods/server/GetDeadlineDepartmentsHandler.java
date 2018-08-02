package com.aplana.sbrf.taxaccounting.web.module.periods.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetDeadlineDepartmentsAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.GetDeadlineDepartmentsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP')")
@Component
public class GetDeadlineDepartmentsHandler extends AbstractActionHandler<GetDeadlineDepartmentsAction, GetDeadlineDepartmentsResult> {

	public GetDeadlineDepartmentsHandler() {
		super(GetDeadlineDepartmentsAction.class);
	}

	@Autowired
	private SecurityService securityService;
	@Autowired
	private DepartmentService departmentService;

	@Override
	public GetDeadlineDepartmentsResult execute(GetDeadlineDepartmentsAction action, ExecutionContext executionContext) throws ActionException {
		TAUserInfo userInfo = securityService.currentUserInfo();
		TaxType taxType = action.getTaxType();
		List<Department> departments = new ArrayList<Department>();
		GetDeadlineDepartmentsResult result = new GetDeadlineDepartmentsResult();
		Department d = null;
        if (userInfo.getUser().hasRole(taxType, TARole.N_ROLE_CONTROL_UNP)) {
			switch (taxType) {
                case NDFL:
					departments.addAll(departmentService.getBADepartments(userInfo.getUser(), action.getTaxType()));
					d = departmentService.getBankDepartment();
					break;
				default:
					break;
			}
		} else { // Контролер НС
			departments.addAll(departmentService.getBADepartments(userInfo.getUser(), action.getTaxType()));
			d = departmentService.getTBDepartments(userInfo.getUser(), taxType).get(0);
		}

		DepartmentPair dep = new DepartmentPair();
		dep.setDepartmentId(d.getId());
		dep.setDepartmentName(d.getName());
		dep.setParentDepartmentId(d.getParentId());
		result.setSelectedDepartment(dep);
		result.setDepartments(departments);
		return result;
	}

	@Override
	public void undo(GetDeadlineDepartmentsAction getDeadlineDepartmentsAction, GetDeadlineDepartmentsResult getDeadlineDepartmentsResult, ExecutionContext executionContext) throws ActionException {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
