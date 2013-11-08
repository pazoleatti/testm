package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client;

import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter.HistoryBusinessFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter.HistoryBusinessFilterView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * User: avanteev
 */
public class HistoryBusinesstModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(HistoryBusinessPresenter.class, HistoryBusinessPresenter.MyView.class, HistoryBusinessView.class, HistoryBusinessPresenter.MyProxy.class);
        bindPresenterWidget(HistoryBusinessFilterPresenter.class, HistoryBusinessFilterPresenter.MyView.class, HistoryBusinessFilterView.class);
    }
}
