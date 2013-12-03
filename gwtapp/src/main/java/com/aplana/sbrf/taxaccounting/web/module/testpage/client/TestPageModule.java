package com.aplana.sbrf.taxaccounting.web.module.testpage.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class TestPageModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bindPresenter(TestPagePresenter.class, TestPagePresenter.MyView.class,
                TestPageView.class, TestPagePresenter.MyProxy.class);
    }
}
