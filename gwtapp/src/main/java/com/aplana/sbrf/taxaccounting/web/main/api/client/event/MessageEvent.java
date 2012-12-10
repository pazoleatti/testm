package com.aplana.sbrf.taxaccounting.web.main.api.client.event;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Отображает сообщение в всплывающем диалоге
 */
public class MessageEvent extends GwtEvent<MessageEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onPopUpMessage(MessageEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, String msg, Throwable throwable) {
		MessageEvent errorEvent = new MessageEvent();
		errorEvent.setMessage(msg);
		errorEvent.setThrowable(throwable);
		source.fireEvent(errorEvent);
	}
	
	public static void fire(HasHandlers source, String msg, List<LogEntry> logEntries) {
		MessageEvent errorEvent = new MessageEvent();
		errorEvent.setMessage(msg);
		errorEvent.setLogEntries(logEntries);	
		source.fireEvent(errorEvent);
	}

	public static void fire(HasHandlers source, String msg) {
		MessageEvent errorEvent = new MessageEvent();
		errorEvent.setMessage(msg);
		source.fireEvent(errorEvent);
	}
	
	private boolean modal;
	
	private String title;

	private String message;
	
	private List<LogEntry> logEntries;

	private Throwable throwable;

	public MessageEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onPopUpMessage(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		this.logEntries = logEntries;
	}

	public boolean isModal() {
		return modal;
	}

	public void setModal(boolean modal) {
		this.modal = modal;
	}

}
