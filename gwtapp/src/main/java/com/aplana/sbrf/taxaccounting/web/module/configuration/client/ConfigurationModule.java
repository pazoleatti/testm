package com.aplana.sbrf.taxaccounting.web.module.configuration.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ConfigurationModule extends AbstractPresenterModule {
	
	@Override
	protected void configure() {
		bindPresenter(ConfigurationPresenter.class,
				ConfigurationPresenter.MyView.class, ConfigurationView.class,
				ConfigurationPresenter.MyProxy.class);
	}
	
}
