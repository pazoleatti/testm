package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class CoreModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		
		install(new DispatchAsyncModule());
		// Presenters
		bindPresenter(FormDataListPresenter.class, FormDataListPresenter.MyView.class,
				FormDataListView.class, FormDataListPresenter.MyProxy.class);

		bindPresenter(FormDataPresenter.class, FormDataPresenter.MyView.class,
				FormDataView.class, FormDataPresenter.MyProxy.class);
	}

}