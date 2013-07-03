package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog.OpenDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog.OpenDialogView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class PeriodsModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(PeriodsPresenter.class, PeriodsPresenter.MyView.class,
				PeriodsView.class, PeriodsPresenter.MyProxy.class);
		bindSingletonPresenterWidget(OpenDialogPresenter.class,
				OpenDialogPresenter.MyView.class, OpenDialogView.class);

	}

}
