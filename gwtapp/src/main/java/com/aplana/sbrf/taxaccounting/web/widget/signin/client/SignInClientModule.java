package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class SignInClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {

		// Биндим презентер для виджета
		bindSingletonPresenterWidget(SignInPresenter.class,
				SignInPresenter.MyView.class, SignInView.class);

		// Удостоверяемся что Binder будет синглетоном,
		// куда бы небыл помещен виджет
		bind(SignInView.Binder.class).in(Singleton.class);

	}

}