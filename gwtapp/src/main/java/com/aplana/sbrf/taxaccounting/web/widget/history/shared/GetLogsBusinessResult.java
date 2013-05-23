package com.aplana.sbrf.taxaccounting.web.widget.history.shared;

import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetLogsBusinessResult implements Result{

	private Map<Integer, String> userNames;
	private List<LogBusiness> logs;

	public List<LogBusiness> getLogs() {
		return logs;
	}

	public void setLogs(List<LogBusiness> logs) {
		this.logs = logs;
	}

	public Map<Integer, String> getUserNames() {
		return userNames;
	}

	public void setUserNames(Map<Integer, String> usersName) {
		this.userNames = usersName;
	}
}
