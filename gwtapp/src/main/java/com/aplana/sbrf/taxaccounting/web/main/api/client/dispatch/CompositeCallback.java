package com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public final class CompositeCallback<T> implements AsyncCallback<T> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static CompositeCallback create() {
		return new CompositeCallback(null);
	}

	public static <T> CompositeCallback<T> create(AsyncCallback<T> callback) {
		return new CompositeCallback<T>(callback);
	}

	@SuppressWarnings("rawtypes")
	private final List<AsyncCallback> callbacks = new ArrayList<AsyncCallback>();

	private CompositeCallback(AsyncCallback<T> callback) {
		if (callback != null) {
			callbacks.add(callback);
		}
	}

	@Override
	public void onFailure(Throwable caught) {
		for (@SuppressWarnings("rawtypes") AsyncCallback callback : callbacks) {
			callback.onFailure(caught);
		}
	}

	@Override
	@SuppressWarnings({"unchecked" })
	public void onSuccess(T result) {
		for (@SuppressWarnings("rawtypes") AsyncCallback callback : callbacks) {
			callback.onSuccess(result);
		}
	}

	public CompositeCallback<T> addCallback(@SuppressWarnings("rawtypes") AsyncCallback callback) {
		callbacks.add(callback);
		return this;
	}

}
