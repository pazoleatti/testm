package com.aplana.sbrf.taxaccounting.migration.web.client;

import com.aplana.sbrf.taxaccounting.migration.web.client.page.MainPagePresenter;
import com.aplana.sbrf.taxaccounting.migration.web.client.page.MainPageView;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import com.gwtplatform.mvp.client.RootPresenter;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

/**
 * @author Alexander Ivanov
 */
public class ClientModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
        bind(TokenFormatter.class).to(ParameterTokenFormatter.class).in(Singleton.class);
        bind(RootPresenter.class).asEagerSingleton();
        bind(PlaceManager.class).to(MigrationPlaceManager.class).in(Singleton.class);

        install(new DispatchAsyncModule());

        bindPresenter(MainPagePresenter.class,
                MainPagePresenter.MyView.class,
                MainPageView.class,
                MainPagePresenter.MyProxy.class);

    }
}