package com.aplana.sbrf.taxaccounting.web.main.api.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Отображает сообщение во всплывающем диалоге
 */
public class MessageEvent extends AbstractMessageEvent<MessageEvent.MyHandler> {

	public interface MyHandler extends EventHandler {
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

	public static void fire(HasHandlers source, String msg) {
		MessageEvent errorEvent = new MessageEvent();
		errorEvent.setMessage(msg);
		source.fireEvent(errorEvent);
	}

    public static void fire(HasHandlers source, Boolean isError, String msg, Throwable throwable) {
        MessageEvent errorEvent = new MessageEvent();
        errorEvent.setMessage(msg);
        errorEvent.setThrowable(throwable);
        errorEvent.setError(isError);
        source.fireEvent(errorEvent);
    }

    public static void fire(HasHandlers source, Boolean isError, String msg) {
        MessageEvent errorEvent = new MessageEvent();
        errorEvent.setMessage(msg);
        errorEvent.setError(isError);
        source.fireEvent(errorEvent);
    }

	private boolean modal;
    private boolean isError = false;

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

	public boolean isModal() {
		return modal;
	}

	public void setModal(boolean modal) {
		this.modal = modal;
	}

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }
}
