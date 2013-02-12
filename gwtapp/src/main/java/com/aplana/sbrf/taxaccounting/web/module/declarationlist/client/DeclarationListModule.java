package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DeclarationListModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(DeclarationListPresenter.class,
				DeclarationListPresenter.MyView.class,	DeclarationListView.class, DeclarationListPresenter.MyProxy.class);
		bindSingletonPresenterWidget(DeclarationFilterPresenter.class,
				DeclarationFilterPresenter.MyView.class, DeclarationFilterView.class);
	}
}
