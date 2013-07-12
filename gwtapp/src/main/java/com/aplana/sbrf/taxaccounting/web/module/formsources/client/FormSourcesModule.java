package com.aplana.sbrf.taxaccounting.web.module.formsources.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class FormSourcesModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(FormSourcesPresenter.class, FormSourcesPresenter.MyView.class,
				FormSourcesView.class, FormSourcesPresenter.MyProxy.class);
	}

}