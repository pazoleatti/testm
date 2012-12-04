package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPagePresenter;
import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPageView;
import com.aplana.sbrf.taxaccounting.web.module.about.client.AboutModule;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminModule;
import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorPagePresenter;
import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorPageView;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataModule;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListClientModule;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeModule;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeNameTokens;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.MainMenuClientModule;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInClientModule;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;


public class ClientModule extends AbstractPresenterModule {
  @Override
  protected void configure() {
	  
    // Default implementation of standard resources
    //install(new DefaultModule(PlaceManager.class));
    
    bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
    bind(TokenFormatter.class).to(ParameterTokenFormatter.class).in(Singleton.class);
    bind(TaRootPresenter.class).asEagerSingleton();
    //bind(GoogleAnalytics.class).to(GoogleAnalyticsImpl.class).in(Singleton.class);
    bind(PlaceManager.class).to(TaPlaceManager.class).in(Singleton.class);
    
	install(new DispatchAsyncModule());
	
	requestStaticInjection(EventBus.class);

	bindConstant().annotatedWith(DefaultPlace.class).to(HomeNameTokens.homePage);
	
    bindPresenter(MainPagePresenter.class, MainPagePresenter.MyView.class,
            MainPageView.class, MainPagePresenter.MyProxy.class);
    bindPresenter(ErrorPagePresenter.class, ErrorPagePresenter.MyView.class,
            ErrorPageView.class, ErrorPagePresenter.MyProxy.class);
    
    install(new HomeModule());
    install(new AboutModule());
    install(new FormDataListClientModule());
    install(new FormDataModule());
    install(new SignInClientModule());
    install(new MainMenuClientModule());
      install(new AdminModule());
  }
}