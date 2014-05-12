package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.web.module.formdata.client.search.FormSearchPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.search.FormSearchView;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersView;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources.SourcesPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.sources.SourcesView;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class FormDataModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(FormDataPresenter.class, FormDataPresenterBase.MyView.class, FormDataView.class, FormDataPresenter.MyProxy.class);
		bindSingletonPresenterWidget(SignersPresenter.class, SignersPresenter.MyView.class, SignersView.class);
		bindSingletonPresenterWidget(DialogPresenter.class, DialogPresenter.MyView.class, DialogView.class);
		bindSingletonPresenterWidget(FormSearchPresenter.class, FormSearchPresenter.MyView.class, FormSearchView.class);
		bindSingletonPresenterWidget(SourcesPresenter.class, SourcesPresenter.MyView.class, SourcesView.class);
	}

}