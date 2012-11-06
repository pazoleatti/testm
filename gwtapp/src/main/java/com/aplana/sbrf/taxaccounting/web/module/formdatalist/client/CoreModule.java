package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class CoreModule extends AbstractPresenterModule {

	@Override
	protected void configure() {

		// Presenters
		bindPresenter(FormDataListPresenter.class, FormDataListPresenter.MyView.class,
				FormDataListView.class, FormDataListPresenter.MyProxy.class);

		bindPresenter(FormDataPresenter.class, FormDataPresenter.MyView.class,
				FormDataView.class, FormDataPresenter.MyProxy.class);
	}

}