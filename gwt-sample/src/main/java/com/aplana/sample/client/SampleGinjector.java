package com.aplana.sample.client;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * @author Vitaliy Samolovskikh
 */
@GinModules(MainModule.class)
public interface SampleGinjector extends Ginjector {
	PlaceManager getPlaceManager();
	@SuppressWarnings("UnusedDeclaration")
	EventBus getEventBus();
	@SuppressWarnings("UnusedDeclaration")
	AsyncProvider<MainPagePresenter> getMainPagePresenter();
}
