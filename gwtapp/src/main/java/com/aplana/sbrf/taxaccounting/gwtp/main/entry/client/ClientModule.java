package com.aplana.sbrf.taxaccounting.gwtp.main.entry.client;

import com.aplana.sbrf.taxaccounting.gwtapp.client.CoreModule;
import com.aplana.sbrf.taxaccounting.gwtp.main.page.client.MainPagePresenter;
import com.aplana.sbrf.taxaccounting.gwtp.main.page.client.MainPageView;
import com.aplana.sbrf.taxaccounting.gwtp.module.about.client.AboutModule;
import com.aplana.sbrf.taxaccounting.gwtp.module.home.client.HomeModule;
import com.aplana.sbrf.taxaccounting.gwtp.module.home.client.HomeNameTokens;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;


public class ClientModule extends AbstractPresenterModule {
  @Override
  protected void configure() {
	  
    // Default implementation of standard resources
    install(new DefaultModule(PlaceManager.class));

	bindConstant().annotatedWith(DefaultPlace.class).to(HomeNameTokens.homePage);
	
    bindPresenter(MainPagePresenter.class, MainPagePresenter.MyView.class,
            MainPageView.class, MainPagePresenter.MyProxy.class);
    
    install(new HomeModule());
    install(new AboutModule());
    install(new CoreModule());
  }
}