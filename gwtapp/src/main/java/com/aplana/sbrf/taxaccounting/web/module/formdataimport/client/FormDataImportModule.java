package com.aplana.sbrf.taxaccounting.web.module.formdataimport.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class FormDataImportModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(FormDataImportPresenter.class,
				FormDataImportPresenter.MyView.class, FormDataImportView.class,
				FormDataImportPresenter.MyProxy.class);

	}
}
