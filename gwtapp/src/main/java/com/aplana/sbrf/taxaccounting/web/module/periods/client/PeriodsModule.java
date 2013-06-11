package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class PeriodsModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(PeriodsPresenter.class, PeriodsPresenter.MyView.class,
				PeriodsView.class, PeriodsPresenter.MyProxy.class);

	}

}
