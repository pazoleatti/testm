package com.aplana.sbrf.taxaccounting.web.module.members.server;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.TARoleService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
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
import java.util.Set;

@Component
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class GetFilterValuesHandler extends AbstractActionHandler<GetFilterValues, FilterValues> {

	@Autowired
	TARoleService taRoleService;
	@Autowired
	DepartmentService departmentService;

	public GetFilterValuesHandler() {
		super(GetFilterValues.class);
	}

	@Override
	public FilterValues execute(GetFilterValues action, ExecutionContext context) throws ActionException {
		FilterValues result = new FilterValues();
		result.setRoles(taRoleService.getAll());
		Set<Integer> depIds = new HashSet<Integer>();
		for (Department dep : departmentService.listAll()) {
			depIds.add(dep.getId());
		}
		result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(depIds).values()));
		return result;
	}

	@Override
	public void undo(GetFilterValues action, FilterValues result, ExecutionContext context) throws ActionException {

	}
}