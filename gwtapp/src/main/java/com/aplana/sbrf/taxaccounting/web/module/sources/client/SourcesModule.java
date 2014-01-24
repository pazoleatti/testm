package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class SourcesModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(SourcesPresenter.class, SourcesPresenter.MyView.class,
				SourcesView.class, SourcesPresenter.MyProxy.class);
	}

}