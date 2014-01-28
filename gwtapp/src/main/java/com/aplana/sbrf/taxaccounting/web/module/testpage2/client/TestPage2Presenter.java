package com.aplana.sbrf.taxaccounting.web.module.testpage2.client;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.testpage2.shared.GetDataAction;
import com.aplana.sbrf.taxaccounting.web.module.testpage2.shared.GetDataResult;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTreeItem;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.List;


public class TestPage2Presenter extends Presenter<TestPage2Presenter.MyView,
        TestPage2Presenter.MyProxy> implements TestPage2UiHandlers {

    @Title("Тестовая страница 2")
    @ProxyCodeSplit
    @NameToken(TestPage2Tokens.TEST_PAGE)
    public interface MyProxy extends ProxyPlace<TestPage2Presenter>, Place {
    }

    private final DispatchAsync dispatcher;

    public interface MyView extends View, HasUiHandlers<TestPage2UiHandlers> {
        void addToTree(MultiSelectTreeItem item, List<TAUser> list);
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

    @Override
    public void getData(final MultiSelectTreeItem item) {
        GetDataAction action = new GetDataAction();
        action.setId(item.getId());
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AsyncCallback<GetDataResult>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Ошибака!");
            }

            @Override
            public void onSuccess(GetDataResult result) {
                getView().addToTree(item, result.getValues());
            }
        }, this));
    }
}
