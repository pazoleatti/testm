package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 * Date: 2013
 */
public class TemplateVersionListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(TemplateVersionListPresenter.class, TemplateVersionListPresenter.MyView.class, TemplateVersionListView.class, TemplateVersionListPresenter.MyProxy.class);
    }
}
