package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class FormDataListModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(FormDataListPresenter.class, FormDataListPresenter.MyView.class,
				FormDataListView.class, FormDataListPresenter.MyProxy.class);
	}

}