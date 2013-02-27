package com.aplana.sbrf.taxaccounting.web.main.api.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.ActionException;

public class ExtActionException extends ActionException {
	private static final long serialVersionUID = 2378347325524891374L;
	
	private List<LogEntry> logEntries;
	
	public ExtActionException() {
		super();
	}

	public ExtActionException(String msg) {
		super(msg);
	}
	
	public ExtActionException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExtActionException(Throwable e) {
		super(e);
	}
	
	public ExtActionException(String msg, List<LogEntry> logEntries, Throwable e) {
		super(msg, e);
		this.logEntries = logEntries;
	}
	
	public ExtActionException(String msg, List<LogEntry> logEntries) {
		super(msg);
		this.logEntries = logEntries;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}	

}
