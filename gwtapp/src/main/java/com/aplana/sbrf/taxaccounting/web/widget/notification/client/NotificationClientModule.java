package com.aplana.sbrf.taxaccounting.web.widget.notification.client;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class NotificationClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {

		// Биндим презентер для виджета
		bindSingletonPresenterWidget(NotificationPresenter.class,
				NotificationPresenter.MyView.class, NotificationView.class);

		// Удостоверяемся что Binder будет синглетоном,
		// куда бы небыл помещен виджет
		bind(NotificationView.Binder.class).in(Singleton.class);

	}

}