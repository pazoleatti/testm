package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class RefBookDataModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(RefBookDataPresenter.class, RefBookDataPresenter.MyView.class,
				RefBookDataView.class, RefBookDataPresenter.MyProxy.class);
		bindSingletonPresenterWidget(EditFormPresenter.class, EditFormPresenter.MyView.class, EditFormView.class);
	}
}
