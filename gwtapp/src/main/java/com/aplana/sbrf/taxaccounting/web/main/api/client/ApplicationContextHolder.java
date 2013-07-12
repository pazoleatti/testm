package com.aplana.sbrf.taxaccounting.web.main.api.client;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

public class ApplicationContextHolder {

	private ApplicationContextHolder(){
		super();
	}
	
	private static EventBus eventBus;
	private static DispatchAsync dispatchAsync;

	public static EventBus getEventBus() {
		return eventBus;
	}

	public static void setEventBus(EventBus eventBus) {
		ApplicationContextHolder.eventBus = eventBus;
	}

	public static DispatchAsync getDispatchAsync() {
		return dispatchAsync;
	}

	public static void setDispatchAsync(DispatchAsync dispatchAsync) {
		ApplicationContextHolder.dispatchAsync = dispatchAsync;
	}
}
