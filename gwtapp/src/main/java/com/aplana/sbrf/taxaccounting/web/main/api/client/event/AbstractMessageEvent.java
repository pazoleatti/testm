package com.aplana.sbrf.taxaccounting.web.main.api.client.event;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

abstract public class AbstractMessageEvent<T extends EventHandler> extends GwtEvent<T> {
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		this.logEntries = logEntries;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	private String title;

	private String message;
	
	private List<LogEntry> logEntries;

	private Throwable throwable;

}
