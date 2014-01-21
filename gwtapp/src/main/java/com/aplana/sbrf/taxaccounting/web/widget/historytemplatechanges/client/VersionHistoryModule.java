package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 */
public class VersionHistoryModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindSingletonPresenterWidget(VersionHistoryPresenter.class,
                VersionHistoryPresenter.MyView.class, VersionHistoryView.class);
    }
}
