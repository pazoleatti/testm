package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class HistoryClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindSingletonPresenterWidget(HistoryPresenter.class,
				HistoryPresenter.MyView.class, HistoryView.class);

		bind(HistoryView.Binder.class).in(Singleton.class);

	}

}