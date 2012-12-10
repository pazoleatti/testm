package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPagePresenter;
import com.aplana.sbrf.taxaccounting.web.main.page.client.MessageDialogPresenter;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public interface ClientGinjectorBase extends Ginjector {
	
	PlaceManager getPlaceManager();

	EventBus getEventBus();

	Provider<MainPagePresenter> getMainPagePresenter();
	
	AsyncProvider<MessageDialogPresenter> getMessageDialogPresenter();
	
	DispatchAsync getDispatchAsync();

}