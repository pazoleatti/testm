package com.aplana.sbrf.taxaccounting.web.module.members.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.FilterValues;
import com.aplana.sbrf.taxaccounting.web.module.members.shared.GetFilterValues;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_OPER')")
public class GetFilterValuesHandler extends AbstractActionHandler<GetFilterValues, FilterValues> {

	@Autowired
	TARoleService taRoleService;
	@Autowired
	DepartmentService departmentService;
	@Autowired
	SecurityService securityService;
	@Autowired
	SourceService departmentFormTypService;

	public GetFilterValuesHandler() {
		super(GetFilterValues.class);
	}

	@Override
	public FilterValues execute(GetFilterValues action, ExecutionContext context) throws ActionException {
		FilterValues result = new FilterValues();
		result.setRoles(taRoleService.getAll());
		TAUser currentUser = securityService.currentUserInfo().getUser();

		Set<Integer> depIds = new HashSet<Integer>();

		if (currentUser.hasRole(TARole.ROLE_ADMIN) || currentUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
			for (Department dep : departmentService.listAll()) {
				depIds.add(dep.getId());
			}
		} else if (currentUser.hasRole(TARole.ROLE_CONTROL)) {

			List<DepartmentFormType> formSrcList = departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.PROPERTY);
			formSrcList.addAll(departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.TRANSPORT));
			formSrcList.addAll(departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.DEAL));
			formSrcList.addAll(departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.VAT));
			formSrcList.addAll(departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.INCOME));

			List<DepartmentFormType> formDstList = departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.PROPERTY);
			formDstList.addAll(departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.TRANSPORT));
			formDstList.addAll(departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.DEAL));
			formDstList.addAll(departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.VAT));
			formDstList.addAll(departmentFormTypService.getDFTSourcesByDepartment(currentUser.getDepartmentId(), TaxType.INCOME));

			for (DepartmentFormType dft : formSrcList) {
				depIds.add(dft.getDepartmentId());
			}
			for (DepartmentFormType dft : formDstList) {
				depIds.add(dft.getDepartmentId());
			}
			depIds.add(currentUser.getDepartmentId());
		} else if (currentUser.hasRole(TARole.ROLE_OPER)) {
			depIds.add(currentUser.getDepartmentId());
		}
		result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(depIds).values()));
		return result;
	}

	@Override
	public void undo(GetFilterValues action, FilterValues result, ExecutionContext context) throws ActionException {

	}
}