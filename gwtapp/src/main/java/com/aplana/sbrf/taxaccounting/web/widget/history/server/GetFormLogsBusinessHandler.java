package com.aplana.sbrf.taxaccounting.web.widget.history.server;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.GetFormLogsBusinessAction;
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
public class GetFormLogsBusinessHandler extends
		AbstractActionHandler<GetFormLogsBusinessAction, GetLogsBusinessResult> {

	public GetFormLogsBusinessHandler() {
		super(GetFormLogsBusinessAction.class);
	}

	@Autowired
	private LogBusinessService logBusinessService;

	@Autowired
	private SecurityService securityService;

	@Override
	public GetLogsBusinessResult execute(GetFormLogsBusinessAction action,
			ExecutionContext context) throws ActionException {

		List<LogBusiness> logs = logBusinessService.getFormLogsBusiness(action.getId());

		GetLogsBusinessResult result = new GetLogsBusinessResult();
		result.setLogs(logs);

		Map<Integer, String> names = new HashMap<Integer, String>();
		for (LogBusiness log : logs) {
			names.put(log.getUserId(), securityService.getUserById(log.getUserId()).getName());
		}
		result.setUserNames(names);

		return result;

	}

	@Override
	public void undo(GetFormLogsBusinessAction action, GetLogsBusinessResult result,
			ExecutionContext context) throws ActionException {
	}

}
