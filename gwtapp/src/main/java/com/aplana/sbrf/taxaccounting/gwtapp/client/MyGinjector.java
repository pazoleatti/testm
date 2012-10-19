package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provider;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

@GinModules({DispatchAsyncModule.class, MyModule.class})
public interface MyGinjector extends Ginjector {
	EventBus getEventBus();

	Provider<FormDataListPresenter> getMainPagePresenter();

	PlaceManager getPlaceManager();

	AsyncProvider<FormDataPresenter> getResponsePresenter();
}