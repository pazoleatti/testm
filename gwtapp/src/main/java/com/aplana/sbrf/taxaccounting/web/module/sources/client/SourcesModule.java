package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.web.module.sources.client.editDialog.EditDeatinationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.editDialog.EditDestinationView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class SourcesModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(SourcesPresenter.class, SourcesPresenter.MyView.class,
				SourcesView.class, SourcesPresenter.MyProxy.class);
        bindSingletonPresenterWidget(EditDeatinationPresenter.class, EditDeatinationPresenter.MyView.class, EditDestinationView.class);
	}

}