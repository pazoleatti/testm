package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Навигирует на страницу ошибки. Следует вызывать в случае фатальной
 * исключительной ситуации, когда уверенности в нормальном состоянии формы после
 * ошибки нет, и дальнейшее продолжение работы с ней не имеет смысла.
 * 
 * @author sgoryachkin
 * 
 */
public class ErrorEvent extends GwtEvent<ErrorEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onError(ErrorEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, String msg, Throwable throwable) {
		ErrorEvent errorEvent = new ErrorEvent();
		errorEvent.setMessage(msg);
		errorEvent.setThrowable(throwable);
		source.fireEvent(errorEvent);
	}

	public static void fire(HasHandlers source, String msg) {
		ErrorEvent errorEvent = new ErrorEvent();
		errorEvent.setMessage(msg);
		source.fireEvent(errorEvent);
	}

	private String message;

	private Throwable throwable;

	public ErrorEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onError(this);
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

}
