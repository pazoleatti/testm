package com.aplana.sbrf.taxaccounting.migration.web.client.page;


import com.aplana.sbrf.taxaccounting.migration.web.shared.StartAction;
import com.aplana.sbrf.taxaccounting.migration.web.shared.StartResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Ivanov
 */
public class MainPagePresenter extends
        Presenter<MainPagePresenter.MyView, MainPagePresenter.MyProxy> implements MigrationUiHandlers {


    public interface MyView extends View, HasUiHandlers<MigrationUiHandlers> {
        void appendText(String msg);
    }

    private final DispatchAsync dispatcher;
    private final PlaceManager placeManager;


    @ProxyCodeSplit
    @NameToken("main")
    public interface MyProxy extends ProxyPlace<MainPagePresenter>, Place {
    }

    @Inject
    public MainPagePresenter(EventBus eventBus, MyView view, MyProxy proxy,
                             PlaceManager placeManager, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        this.placeManager = placeManager;
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void start(List<Long> selectedList) {
        dispatcher.execute(new StartAction(selectedList), new AsyncCallback<StartResult>() {
            @Override
            public void onFailure(Throwable caught) {
                getView().appendText(caught.getMessage());
            }

            @Override
            public void onSuccess(StartResult result) {
                getView().appendText("Найдено " + result.getExemplarList().size() + " актуальный экземпляра.");
                LinkedHashMap<String, String> files = (LinkedHashMap<String, String>) result.getFiles();
                for (Map.Entry<String, String> entry : files.entrySet()){
                    getView().appendText(entry.getKey() + "\n\r");
                    getView().appendText(entry.getValue() + "\n\r");
                }
            }
        });
    }

    @Override
    protected void revealInParent() {
        RevealRootContentEvent.fire(this, this);
    }
}
