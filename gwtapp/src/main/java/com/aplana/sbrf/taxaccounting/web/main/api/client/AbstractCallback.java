package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.aplana.sbrf.taxaccounting.web.main.entry.client.ClientGinjector;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.DelayedBindRegistry;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

public abstract class AbstractCallback<T> implements AsyncCallback<T>,
		HasHandlers {
	
	private static EventBus eventBus = ((ClientGinjector)DelayedBindRegistry.getGinjector()).getEventBus();
	
	public AbstractCallback(){
		LockInteractionEvent.fire(this, true);
	}
	
	@Override
	public void onSuccess(T result) {
		LockInteractionEvent.fire(this, false);
	}

	@Override
	public void onFailure(Throwable throwable) {
		throwable.printStackTrace();
		Window.alert(throwable.getMessage());
		LockInteractionEvent.fire(this, false);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		System.out.println("EB:" + eventBus);
		eventBus.fireEventFromSource(event, this);
	}
}