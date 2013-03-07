package com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.ActionException;

public class TaActionException extends ActionException {
	private static final long serialVersionUID = 2378347325524891374L;
	
	private List<LogEntry> logEntries;
	
	private String trace;
	
	public TaActionException() {
		super();
	}

	public TaActionException(String msg) {
		super(msg);
	}
	
	public TaActionException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public TaActionException(Throwable e) {
		super(e);
	}
	
	public TaActionException(String msg, List<LogEntry> logEntries, Throwable e) {
		super(msg, e);
		this.logEntries = logEntries;
	}
	
	public TaActionException(String msg, List<LogEntry> logEntries) {
		super(msg);
		this.logEntries = logEntries;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}	

}
