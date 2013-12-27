package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class MessageOnFailureCallback<T> implements AsyncCallback<T> {

	private HasHandlers hasHandlers;

	private final AsyncCallback<T> callback;
	private boolean showLogOnly;

	public static <T> AsyncCallback<T> create(HasHandlers hasHandlers) {
		return new MessageOnFailureCallback<T>(false, null, hasHandlers);
	}

	public static <T> AsyncCallback<T> create(AsyncCallback<T> callback, HasHandlers hasHandlers) {
		return new MessageOnFailureCallback<T>(false, callback, hasHandlers);
	}

	/**
	 * Флаг showLogOnly актуален только когда ошибка вызвана присутствием
	 * сообщений об ошибке в логе. В других случаях всё равно нужно отобразить
	 * диалог.
	 * 
	 * @param showLogOnly
	 * @return
	 */
	public static <T> AsyncCallback<T> create(boolean showLogOnly, HasHandlers hasHandlers) {
		return new MessageOnFailureCallback<T>(showLogOnly, null, hasHandlers);
	}

	public static <T> AsyncCallback<T> create(boolean showLogOnly,
			AsyncCallback<T> callback, HasHandlers hasHandlers) {
		return new MessageOnFailureCallback<T>(showLogOnly, callback, hasHandlers);
	}

	private MessageOnFailureCallback(boolean showLogOnly,
			AsyncCallback<T> callback, HasHandlers hasHandlers) {
		this.callback = callback;
		this.showLogOnly = showLogOnly;
		this.hasHandlers = hasHandlers;
	}

	@Override
	public void onFailure(Throwable caught) {
		if (callback != null) {
			callback.onFailure(caught);
		}
		if (caught instanceof TaActionException) {
			String uuid = ((TaActionException) caught).getUuid();
			if (uuid != null) {
				LogAddEvent.fire(hasHandlers, ((TaActionException) caught).getUuid());
				// Флаг showLogOnly актуален только когда ошибка вызвана
				// присутствием
				// сообщений об ошибке в логе. В других случаях всё равно нужно
				// отобразить диалог.
				if (!showLogOnly) {
					MessageEvent.fire(hasHandlers, caught.getLocalizedMessage(), caught);
				}
			} else {
				MessageEvent.fire(hasHandlers, caught.getLocalizedMessage(), caught);
			}
		} else {
			MessageEvent.fire(hasHandlers, caught.getLocalizedMessage(), caught);
		}
	}

	@Override
	public void onSuccess(T result) {
		if (callback != null) {
			callback.onSuccess(result);
		}
	}
}


