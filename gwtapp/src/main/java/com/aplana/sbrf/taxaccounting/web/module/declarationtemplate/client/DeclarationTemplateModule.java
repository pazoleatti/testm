package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DeclarationTemplateModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(DeclarationTemplateListPresenter.class, DeclarationTemplateListPresenter.MyView.class,
				DeclarationTemplateListView.class, DeclarationTemplateListPresenter.MyProxy.class);
	}

}