package com.aplana.sbrf.taxaccounting.web.widget.signin.server;

import com.aplana.sbrf.taxaccounting.model.TARole;
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

		GetUserInfoResult result = new GetUserInfoResult();
		result.setRoleAnddepartment(department.getName());
		result.setUserName(user.getName());

        StringBuilder roles = new StringBuilder(user.getName());
        roles.append("\n");
        for (int i = 0; i < user.getRoles().size(); i++) {
            TARole item = user.getRoles().get(i);
            roles.append(item.getName());
            if (i < user.getRoles().size() - 1) {
                roles.append("; ");
            }
        }
        result.setHint(roles.toString());
		return result;
	}

	@Override
	public void undo(GetUserInfoAction action, GetUserInfoResult result,
			ExecutionContext context) throws ActionException {
	}
}
