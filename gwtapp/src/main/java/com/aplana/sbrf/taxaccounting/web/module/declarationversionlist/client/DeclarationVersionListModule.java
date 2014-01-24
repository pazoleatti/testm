package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 */
public class DeclarationVersionListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(DeclarationVersionListPresenter.class, DeclarationVersionListPresenter.MyView.class, DeclarationVersionListView.class,
                DeclarationVersionListPresenter.MyProxy.class);
    }
}
