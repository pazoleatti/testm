package com.aplana.sbrf.taxaccounting.migration.web.client;

import com.aplana.sbrf.taxaccounting.migration.web.client.page.MainPagePresenter;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * @author Alexander Ivanov
 */
@GinModules({ ClientModule.class , DispatchAsyncModule.class})
public interface MyGinjector extends Ginjector {
    PlaceManager getPlaceManager();
    EventBus getEventBus();
    AsyncProvider<MainPagePresenter> getMainPagePresenter();
}
