package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.AssignDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.AssignDialogView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class SourcesModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(SourcesPresenter.class, SourcesPresenter.MyView.class, SourcesView.class, SourcesPresenter.MyProxy.class);
        bindPresenterWidget(AssignDialogPresenter.class, AssignDialogPresenter.MyView.class, AssignDialogView.class);
    }

}