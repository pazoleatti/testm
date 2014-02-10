package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow.client.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow.client.DialogView;
import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class MainMenuClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {

		// Биндим презентер для виджета
		bindSingletonPresenterWidget(MainMenuPresenter.class,
				MainMenuPresenter.MyView.class, MainMenu.class);

		// Удостоверяемся что Binder будет синглетоном,
		// куда бы небыл помещен виджет
		bind(MainMenu.Binder.class).in(Singleton.class);

		bindSingletonPresenterWidget(DialogPresenter.class, DialogPresenter.MyView.class, DialogView.class);
	}

}