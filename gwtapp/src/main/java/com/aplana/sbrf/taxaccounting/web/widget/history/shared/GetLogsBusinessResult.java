package com.aplana.sbrf.taxaccounting.web.widget.history.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetLogsBusinessResult implements Result{

	private List<LogBusinessClient> logs;

	public List<LogBusinessClient> getLogs() {
		return logs;
	}

	public void setLogs(List<LogBusinessClient> logs) {
		this.logs = logs;
	}
}
