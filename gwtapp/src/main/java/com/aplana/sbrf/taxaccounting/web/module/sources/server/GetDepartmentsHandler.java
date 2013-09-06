package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentsAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetDepartmentsResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP')")
public class GetDepartmentsHandler extends AbstractActionHandler<GetDepartmentsAction, GetDepartmentsResult> {
	
	@Autowired 
	private SecurityService securityService;

	@Autowired
	private DepartmentService departmentService;

    public GetDepartmentsHandler() {
        super(GetDepartmentsAction.class);
    }

    @Override
    public GetDepartmentsResult execute(GetDepartmentsAction action, ExecutionContext context) throws ActionException {
		GetDepartmentsResult result = new GetDepartmentsResult();
		TAUserInfo userInfo  = securityService.currentUserInfo();
		if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)){
			result.setDepartments(departmentService.listDepartments());
			result.setAvailableDepartments(null);
		} else if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL)){
			Set<Integer> availableDepartments = new HashSet<Integer>(); 
			availableDepartments.add(userInfo.getUser().getDepartmentId());
			for (Department department : departmentService.getAllChildren(userInfo.getUser().getDepartmentId())) {
				availableDepartments.add(department.getId());
			}
			result.setDepartments(new ArrayList<Department>(departmentService.getRequiredForTreeDepartments(availableDepartments).values()));
			result.setAvailableDepartments(availableDepartments);
		}
		return result;
    }

    @Override
    public void undo(GetDepartmentsAction action, GetDepartmentsResult result, ExecutionContext context) throws ActionException {
        // Nothing!
    }
}
