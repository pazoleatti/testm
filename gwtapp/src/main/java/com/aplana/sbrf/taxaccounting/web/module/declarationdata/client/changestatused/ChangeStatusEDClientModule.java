package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ChangeStatusEDClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindSingletonPresenterWidget(ChangeStatusEDPresenter.class,
                ChangeStatusEDPresenter.MyView.class, ChangeStatusEDView.class);

		bind(ChangeStatusEDPresenter.Binder.class).in(Singleton.class);
	}

}