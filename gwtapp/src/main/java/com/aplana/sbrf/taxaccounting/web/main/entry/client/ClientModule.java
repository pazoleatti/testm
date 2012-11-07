package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPagePresenter;
import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPageView;
import com.aplana.sbrf.taxaccounting.web.module.about.client.AboutModule;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataModule;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListClientModule;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeModule;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeNameTokens;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.MainMenuClientModule;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInClientModule;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;


public class ClientModule extends AbstractPresenterModule {
  @Override
  protected void configure() {
	  
    // Default implementation of standard resources
    install(new DefaultModule(PlaceManager.class));
    
	install(new DispatchAsyncModule());

	bindConstant().annotatedWith(DefaultPlace.class).to(HomeNameTokens.homePage);
	
    bindPresenter(MainPagePresenter.class, MainPagePresenter.MyView.class,
            MainPageView.class, MainPagePresenter.MyProxy.class);
    
    install(new HomeModule());
    install(new AboutModule());
    install(new FormDataListClientModule());
    install(new FormDataModule());
    install(new SignInClientModule());
    install(new MainMenuClientModule());
  }
}