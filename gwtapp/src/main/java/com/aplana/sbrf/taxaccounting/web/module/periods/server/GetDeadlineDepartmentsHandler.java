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

@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
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
		if (userInfo.getUser().hasRole("ROLE_CONTROL_UNP")) {
			switch (taxType) {
				case PROPERTY:
				case TRANSPORT:
                case LAND:
					departments.addAll(departmentService.getAllChildren(action.getDepartment().getDepartmentId()));
					for (Department dep : departments) {
						if (dep.getType() == DepartmentType.TERR_BANK) {
							d = dep;
							break;
						}
					}
					break;
				case INCOME:
				case DEAL:
				case VAT:
                case MARKET:
                case ETR:
                case NDFL:
                case PFR:
					departments.addAll(departmentService.getBADepartments(userInfo.getUser()));
					d = departmentService.getBankDepartment();
					break;
				default:
					break;
			}
		} else { // Контролер НС
			departments.addAll(departmentService.getBADepartments(userInfo.getUser()));
			d = departmentService.getTBDepartments(userInfo.getUser()).get(0);
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
