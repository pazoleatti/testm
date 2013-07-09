package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class ErrorOnFailureCallback<T> implements AsyncCallback<T>{

	private HasHandlers hasHandlers;

	private final AsyncCallback<T> callback;

	public static <T> AsyncCallback<T> create(HasHandlers hasHandlers) {		
		return new ErrorOnFailureCallback<T>(null, hasHandlers);
	}

	public static <T> AsyncCallback<T> create(AsyncCallback<T> callback, HasHandlers hasHandlers) {
		return new ErrorOnFailureCallback<T>(callback, hasHandlers);
	}

	private ErrorOnFailureCallback(AsyncCallback<T> callback, HasHandlers hasHandlers) {
		this.callback = callback;
		this.hasHandlers = hasHandlers;
	}

	@Override
	public void onFailure(Throwable caught) {
		if (callback != null) {
			callback.onFailure(caught);
		}
		if (caught instanceof TaActionException) {
			LogAddEvent.fire(hasHandlers,
					((TaActionException) caught).getLogEntries());

		}
		ErrorEvent.fire(hasHandlers, caught.getLocalizedMessage(), caught);
	}

	@Override
	public void onSuccess(T result) {
		if (callback != null) {
			callback.onSuccess(result);
		}
	}



}
