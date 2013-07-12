package com.aplana.sbrf.taxaccounting.web.module.about.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class AboutModule extends AbstractPresenterModule {
	
  @Override
  protected void configure() {
    bindPresenter(AboutPresenter.class, AboutPresenter.MyView.class,
        AboutView.class, AboutPresenter.MyProxy.class);
  }
}