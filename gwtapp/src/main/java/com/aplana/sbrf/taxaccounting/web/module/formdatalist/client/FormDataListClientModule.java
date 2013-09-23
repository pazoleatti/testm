package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create.CreateFormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create.CreateFormDataView;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterFormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterFormDataView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class FormDataListClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		
		bindPresenter(FormDataListPresenter.class, FormDataListPresenter.MyView.class, FormDataListView.class, FormDataListPresenter.MyProxy.class);
		bindSingletonPresenterWidget(FilterFormDataPresenter.class, FilterFormDataPresenter.MyView.class, FilterFormDataView.class);
		bindSingletonPresenterWidget(CreateFormDataPresenter.class, CreateFormDataPresenter.MyView.class, CreateFormDataView.class);

	}

}