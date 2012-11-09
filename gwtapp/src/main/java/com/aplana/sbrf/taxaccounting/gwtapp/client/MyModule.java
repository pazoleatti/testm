package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;

public class MyModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		// Default implementation of standard resources
		install(new DefaultModule(MyPlaceManager.class));

		// Presenters
		bindPresenter(FormDataListPresenter.class, FormDataListPresenter.MyView.class,
				FormDataListView.class, FormDataListPresenter.MyProxy.class);

		bindPresenter(FormDataPresenter.class, FormDataPresenter.MyView.class,
				FormDataView.class, FormDataPresenter.MyProxy.class);
	}

}