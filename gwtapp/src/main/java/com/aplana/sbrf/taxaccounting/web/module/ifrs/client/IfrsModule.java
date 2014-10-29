package com.aplana.sbrf.taxaccounting.web.module.ifrs.client;

import com.aplana.sbrf.taxaccounting.web.module.ifrs.client.create.CreateIfrsDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.client.create.CreateIfrsDataView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by lhaziev on 22.10.2014.
 */
public class IfrsModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(IfrsPresenter.class, IfrsPresenter.MyView.class, IfrsView.class, IfrsPresenter.MyProxy.class);
        bindSingletonPresenterWidget(CreateIfrsDataPresenter.class, CreateIfrsDataPresenter.MyView.class, CreateIfrsDataView.class);
    }
}
