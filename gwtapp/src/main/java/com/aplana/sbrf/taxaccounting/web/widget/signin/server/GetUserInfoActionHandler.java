package com.aplana.sbrf.taxaccounting.web.widget.signin.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoAction;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;

@Component
public class GetUserInfoActionHandler extends AbstractActionHandler<GetUserInfoAction, GetUserInfoResult>{
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private DepartmentDao departmentDao;

	public GetUserInfoActionHandler() {
		super(GetUserInfoAction.class);
	}

	@Override
	public GetUserInfoResult execute(GetUserInfoAction action, ExecutionContext context) throws ActionException {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String login = auth.getName();
		TAUser user = userDao.getUser(login);
		Department department = departmentDao.getDepartment(user.getDepartmentId());
		StringBuilder name = new StringBuilder(login);
		name.append(" (").append(user.getName());
		if (department != null) {
			name.append(" - ")
				.append(department.getType().getLabel())
				.append(" \"")
				.append(department.getName())
				.append("\"");
		}
		name.append(")");
		GetUserInfoResult result = new GetUserInfoResult();
		result.setUserName(name.toString());
		return result;
	}

	@Override
	public void undo(GetUserInfoAction action, GetUserInfoResult result,
			ExecutionContext context) throws ActionException {
	}
}
