package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog.DialogView;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class FormDataListClientModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(FormDataListPresenter.class, 
				FormDataListPresenter.MyView.class,	FormDataListView.class, FormDataListPresenter.MyProxy.class);
		bindSingletonPresenterWidget(FilterPresenter.class, 
				FilterPresenter.MyView.class, FilterView.class);
		bindSingletonPresenterWidget(DialogPresenter.class, DialogPresenter.MyView.class, DialogView.class);

	}

}