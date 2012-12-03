package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class FormDataModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(FormDataPresenter.class, FormDataPresenterBase.MyView.class, FormDataView.class, FormDataPresenter.MyProxy.class);
	}

}