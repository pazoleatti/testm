package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 */
public class VersionHistoryModule extends AbstractPresenterModule {
    @Override
    protected void configure() {

        bindPresenterWidget(DeclarationVersionHistoryPresenter.class, DeclarationVersionHistoryPresenter.MyView.class,
                DeclarationVersionHistoryPresenter.MyView.class);

        bindPresenterWidget(DeclarationHistoryPresenter.class, DeclarationHistoryPresenter.MyView.class,
                DeclarationHistoryPresenter.MyView.class);

        bindPresenterWidget(FormHistoryPresenter.class, FormHistoryPresenter.MyView.class,
                FormHistoryPresenter.MyView.class);

        bindPresenterWidget(FormVersionHistoryPresenter.class, FormVersionHistoryPresenter.MyView.class,
                FormVersionHistoryPresenter.MyView.class);
    }
}
