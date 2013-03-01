package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.ActionException;

public class LogActionException extends ActionException {
	private static final long serialVersionUID = 2378347325524891374L;
	
	private List<LogEntry> logEntries;
	
	public LogActionException() {
		super();
	}

	public LogActionException(String msg) {
		super(msg);
	}
	
	public LogActionException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public LogActionException(Throwable e) {
		super(e);
	}
	
	public LogActionException(String msg, List<LogEntry> logEntries, Throwable e) {
		super(msg, e);
		this.logEntries = logEntries;
	}
	
	public LogActionException(String msg, List<LogEntry> logEntries) {
		super(msg);
		this.logEntries = logEntries;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}	

}
