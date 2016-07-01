package com.aplana.sbrf.taxaccounting.web.widget.history.server;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;
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
            if (userService.existsUser(log.getUserLogin())) {
                business.setUserName(userService.getUser(log.getUserLogin()).getName());
            } else {
                business.setUserName(log.getUserLogin());
            }
            business.setDepartmentName(log.getDepartmentName());
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
