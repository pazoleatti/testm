package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.aplana.sbrf.taxaccounting.web.main.entry.client.ClientGinjector;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.DelayedBindRegistry;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

public class LockScrCallback<T> implements AsyncCallback<T>, HasHandlers {

	// TODO: Почему то не получается использовать @Inject для статических полей.
	// Надо разобраться.
	// Пока ворк эраунд - получение инжектора руками и установка значения.
	private static final EventBus EVENT_BUS = ((ClientGinjector) DelayedBindRegistry
			.getGinjector()).getEventBus();
	
	private final AsyncCallback<T> callback;

	public static <T> AsyncCallback<T> create() {
		return new LockScrCallback<T>(true, null);
	}
	
	public static <T> AsyncCallback<T> create(AsyncCallback<T> callback) {
		return new LockScrCallback<T>(true, callback);
	}
	
	public static <T> AsyncCallback<T> create(boolean lockImmediately) {
		return new LockScrCallback<T>(lockImmediately, null);
	}
	
	public static <T> AsyncCallback<T> create(boolean lockImmediately, AsyncCallback<T> callback) {
		return new LockScrCallback<T>(lockImmediately, callback);
	}
	
	
	private LockScrCallback(boolean lockImmediately, AsyncCallback<T> callback) {
		LockInteractionEvent.fire(this, true);
		this.callback = callback;
	}

	@Override
	public void onFailure(Throwable caught) {
		if (callback != null) {
			callback.onFailure(caught);
		}
		LockInteractionEvent.fire(this, false);
	}

	@Override
	public void onSuccess(T result) {
		if (callback != null) {
			callback.onSuccess(result);
		}
		LockInteractionEvent.fire(this, false);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		EVENT_BUS.fireEventFromSource(event, this);
	}

}
