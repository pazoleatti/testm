package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.FilterDeclarationTemplatePresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter.FilterDeclarationTemplateView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DeclarationTemplateModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(DeclarationTemplateListPresenter.class, DeclarationTemplateListPresenter.MyView.class,
				DeclarationTemplateListView.class, DeclarationTemplateListPresenter.MyProxy.class);
		bindPresenter(DeclarationTemplatePresenter.class, DeclarationTemplatePresenter.MyView.class,
                DeclarationTemplateView.class, DeclarationTemplatePresenter.MyProxy.class);
        bindPresenterWidget(FilterDeclarationTemplatePresenter.class, FilterDeclarationTemplatePresenter.MyView.class, FilterDeclarationTemplateView.class);
	}

}