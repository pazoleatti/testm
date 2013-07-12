package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

public final class LockScrCallback<T> implements AsyncCallback<T>{

	private HasHandlers hasHandlers;
	
	private final AsyncCallback<T> callback;

	public static <T> AsyncCallback<T> create(HasHandlers hasHandlers) {
		return new LockScrCallback<T>(true, null, hasHandlers);
	}
	
	public static <T> AsyncCallback<T> create(AsyncCallback<T> callback, HasHandlers hasHandlers) {
		return new LockScrCallback<T>(true, callback, hasHandlers);
	}
	
	public static <T> AsyncCallback<T> create(boolean lockImmediately, HasHandlers hasHandlers) {
		return new LockScrCallback<T>(lockImmediately, null, hasHandlers);
	}
	
	public static <T> AsyncCallback<T> create(boolean lockImmediately, AsyncCallback<T> callback, HasHandlers hasHandlers) {
		return new LockScrCallback<T>(lockImmediately, callback, hasHandlers);
	}
	
	
	private LockScrCallback(boolean lockImmediately, AsyncCallback<T> callback, HasHandlers hasHandlers) {
		LockInteractionEvent.fire(hasHandlers, true);
		this.hasHandlers = hasHandlers; 
		this.callback = callback;
	}

	@Override
	public void onFailure(Throwable caught) {
		if (callback != null) {
			callback.onFailure(caught);
		}
		LockInteractionEvent.fire(hasHandlers, false);
	}

	@Override
	public void onSuccess(T result) {
		if (callback != null) {
			callback.onSuccess(result);
		}
		LockInteractionEvent.fire(hasHandlers, false);
	}



}
