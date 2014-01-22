package com.aplana.sbrf.taxaccounting.web.module.testpage2.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class TestPage2Module extends AbstractPresenterModule {

    @Override
    protected void configure() {
        bindPresenter(TestPage2Presenter.class, TestPage2Presenter.MyView.class,
                TestPage2View.class, TestPage2Presenter.MyProxy.class);
    }
}
