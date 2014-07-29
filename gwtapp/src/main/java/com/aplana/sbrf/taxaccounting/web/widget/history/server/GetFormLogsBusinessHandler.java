package com.aplana.sbrf.taxaccounting.web.widget.history.server;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.GetFormLogsBusinessAction;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.GetLogsBusinessResult;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.LogBusinessClient;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GetFormLogsBusinessHandler extends
		AbstractActionHandler<GetFormLogsBusinessAction, GetLogsBusinessResult> {

	public GetFormLogsBusinessHandler() {
		super(GetFormLogsBusinessAction.class);
	}

	@Autowired
	private LogBusinessService logBusinessService;

	@Autowired
	private TAUserService userService;

	@Autowired
	private DepartmentService departmentService;

	@Override
	public GetLogsBusinessResult execute(GetFormLogsBusinessAction action,
			ExecutionContext context) throws ActionException {

		List<LogBusiness> logs = logBusinessService.getFormLogsBusiness(action.getId(),
                action.getFilter().getSearchOrdering(), action.getFilter().isAscSorting());

		GetLogsBusinessResult result = new GetLogsBusinessResult();
        ArrayList<LogBusinessClient> logBusinessClients = new ArrayList<LogBusinessClient>(logs.size());

		for (LogBusiness log : logs) {
            //TODO dloshkarev: можно сразу получать список а не выполнять запросы в цикле
            LogBusinessClient business = new LogBusinessClient(log);
            business.setUserName(userService.getUser(log.getUserLogin()).getName());
            business.setDepartmentName(departmentService.getParentsHierarchyShortNames(log.getDepartmentId()));
            logBusinessClients.add(business);
		}
        result.setLogs(logBusinessClients);

		return result;

	}

	@Override
	public void undo(GetFormLogsBusinessAction action, GetLogsBusinessResult result,
			ExecutionContext context) throws ActionException {
	}

}
