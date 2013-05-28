package com.aplana.sbrf.taxaccounting.web.module.formdataimport.shared;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class FormDataImportResult implements Result {
	private static final long serialVersionUID = 5140717267547423986L;
	
	private List<LogEntry> logEntries;

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		this.logEntries = logEntries;
	}
}