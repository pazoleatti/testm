package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class LogAreaClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {

		// Биндим презентер для виджета
		bindSingletonPresenterWidget(LogAreaPresenter.class,
				LogAreaPresenter.MyView.class, LogAreaView.class);

		// Удостоверяемся что Binder будет синглетоном,
		// куда бы небыл помещен виджет
		bind(LogAreaView.Binder.class).in(Singleton.class);

	}

}