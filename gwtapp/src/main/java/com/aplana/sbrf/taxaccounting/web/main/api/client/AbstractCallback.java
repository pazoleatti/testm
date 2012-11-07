package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public abstract class AbstractCallback<T> implements AsyncCallback<T>,
		HasHandlers {
	@Inject
	private static EventBus eventBus;

	@Override
	public void onFailure(Throwable throwable) {
		throwable.printStackTrace();
		Window.alert(throwable.getMessage());
		// ShowMessageEvent.fire(this, "Oops! Something went wrong!");
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		eventBus.fireEventFromSource(event, this);
	}
}