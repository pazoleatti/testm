package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client;

import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client.history.VersionHistoryPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client.history.VersionHistoryView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 * Date: 2013
 */
public class TemplateVersionListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(TemplateVersionListPresenter.class, TemplateVersionListPresenter.MyView.class, TemplateVersionListView.class, TemplateVersionListPresenter.MyProxy.class);
        bindSingletonPresenterWidget(VersionHistoryPresenter.class, VersionHistoryPresenter.MyView.class, VersionHistoryView.class);
    }
}
