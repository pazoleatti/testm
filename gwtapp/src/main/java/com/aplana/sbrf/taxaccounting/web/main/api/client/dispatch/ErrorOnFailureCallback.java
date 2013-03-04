package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.aplana.sbrf.taxaccounting.web.main.entry.client.ClientGinjector;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.DelayedBindRegistry;

public class ErrorOnFailureCallback<T> implements AsyncCallback<T>,
		HasHandlers {

	// TODO: Почему то не получается использовать @Inject для статических полей.
	// Надо разобраться.
	// Пока ворк эраунд - получение инжектора руками и установка значения.
	private static EventBus EVENT_BUS = ((ClientGinjector) DelayedBindRegistry
			.getGinjector()).getEventBus();

	private final AsyncCallback<T> callback;

	public static <T> AsyncCallback<T> create() {
		return new ErrorOnFailureCallback<T>(null);
	}

	public static <T> AsyncCallback<T> create(AsyncCallback<T> callback) {
		return new ErrorOnFailureCallback<T>(callback);
	}

	private ErrorOnFailureCallback(AsyncCallback<T> callback) {
		this.callback = callback;

	}

	@Override
	public void onFailure(Throwable caught) {
		if (callback != null) {
			callback.onFailure(caught);
		}
		if (caught instanceof TaActionException) {
			LogAddEvent.fire(this,
					((TaActionException) caught).getLogEntries());

		}
		ErrorEvent.fire(this, caught.getLocalizedMessage(), caught);
	}

	@Override
	public void onSuccess(T result) {
		if (callback != null) {
			callback.onSuccess(result);
		}
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		EVENT_BUS.fireEventFromSource(event, this);
	}

}
