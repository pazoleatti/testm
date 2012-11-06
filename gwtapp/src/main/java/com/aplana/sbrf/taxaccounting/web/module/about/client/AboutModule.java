package com.aplana.sbrf.taxaccounting.web.module.about.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class AboutModule extends AbstractPresenterModule {
	
  @Override
  protected void configure() {
    bindPresenter(AboutPagePresenter.class, AboutPagePresenter.MyView.class,
        AboutPageView.class, AboutPagePresenter.MyProxy.class);
    bindPresenter(ContactPagePresenter.class, ContactPagePresenter.MyView.class,
            ContactPageView.class, ContactPagePresenter.MyProxy.class);
  }
  
}