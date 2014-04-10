package com.aplana.sbrf.taxaccounting.web.widget.history.server;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.GetDeclarationLogsBusinessAction;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.GetLogsBusinessResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetDeclarationLogsBusinessHandler extends
		AbstractActionHandler<GetDeclarationLogsBusinessAction, GetLogsBusinessResult> {

	public GetDeclarationLogsBusinessHandler() {
		super(GetDeclarationLogsBusinessAction.class);
	}

	@Autowired
	private LogBusinessService logBusinessService;

	@Autowired
	private TAUserService userService;

	@Autowired
	private DepartmentService departmentService;

	@Override
	public GetLogsBusinessResult execute(GetDeclarationLogsBusinessAction action,
			ExecutionContext context) throws ActionException {

		List<LogBusiness> logs = logBusinessService.getDeclarationLogsBusiness(action.getId());

		GetLogsBusinessResult result = new GetLogsBusinessResult();
		result.setLogs(logs);

		Map<Integer, String> names = new HashMap<Integer, String>();
		Map<Integer, String> departments = new HashMap<Integer, String>();
		for (LogBusiness log : logs) {
            //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
			names.put(log.getUserId(), userService.getUser(log.getUserId()).getName());
			departments.put(log.getDepartmentId(), departmentService.getParentsHierarchyShortNames(log.getDepartmentId()));
		}
		result.setUserNames(names);
		result.setUserDepartments(departments);

		return result;

	}

	@Override
	public void undo(GetDeclarationLogsBusinessAction action, GetLogsBusinessResult result,
			ExecutionContext context) throws ActionException {
	}

}
