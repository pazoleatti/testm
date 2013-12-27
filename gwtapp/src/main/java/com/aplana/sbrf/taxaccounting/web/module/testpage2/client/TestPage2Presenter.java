package com.aplana.sbrf.taxaccounting.web.module.testpage2.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;


public class TestPage2Presenter extends Presenter<TestPage2Presenter.MyView,
        TestPage2Presenter.MyProxy> implements TestPage2UiHandlers {
    @ProxyCodeSplit
    @NameToken(TestPage2Tokens.TEST_PAGE)
    public interface MyProxy extends ProxyPlace<TestPage2Presenter>, Place {
    }

    private final DispatchAsync dispatcher;

    public interface MyView extends View, HasUiHandlers<TestPage2UiHandlers> {
    }

    @Inject
    public TestPage2Presenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                              DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
    }
}
