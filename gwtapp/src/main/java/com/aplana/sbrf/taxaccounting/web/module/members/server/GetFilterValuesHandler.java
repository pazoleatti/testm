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

import java.util.*;

@Component
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN', 'N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
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

        TAUser currentUser = securityService.currentUserInfo().getUser();

        List<TARole> allRoles = taRoleService.getAll();

        result.setRoles(allRoles);

		Set<Integer> depIds = new HashSet<Integer>();
        depIds.addAll(departmentService.getBADepartmentIds(currentUser));

		if (currentUser.hasRole(TARole.N_ROLE_ADMIN)) {
            depIds.addAll(departmentService.listIdAll());
        } else {
            depIds.addAll(departmentService.getBADepartmentIds(currentUser));
        }
        result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(depIds).values()));
		return result;
	}

	@Override
	public void undo(GetFilterValues action, FilterValues result, ExecutionContext context) throws ActionException {

	}
}