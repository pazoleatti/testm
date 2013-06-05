package com.aplana.sbrf.taxaccounting.web.widget.signin.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoAction;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Component
public class GetUserInfoActionHandler extends AbstractActionHandler<GetUserInfoAction, GetUserInfoResult>{
	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private SecurityService securityService;

	public GetUserInfoActionHandler() {
		super(GetUserInfoAction.class);
	}

	@Override
	public GetUserInfoResult execute(GetUserInfoAction action, ExecutionContext context) throws ActionException {
		TAUser user = securityService.currentUserInfo().getUser();
		Department department = departmentService.getDepartment(user.getDepartmentId());

		StringBuilder roleAndDepartment = new StringBuilder(user.getName());
		if (department != null) {
			roleAndDepartment.append(" - ")
				.append(department.getShortName());
		}
		GetUserInfoResult result = new GetUserInfoResult();
		result.setUserName(user.getLogin());
		result.setRoleAnddepartment(roleAndDepartment.toString());
		return result;
	}

	@Override
	public void undo(GetUserInfoAction action, GetUserInfoResult result,
			ExecutionContext context) throws ActionException {
	}
}
