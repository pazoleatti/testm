package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.HashMap;
import java.util.Map;

/**
 * Презентер списка справочников, для конфигуратора.
 *
 * @author Fail Mukhametdinov
 */
public class AdminRefBookListPresenter extends AbstractRefBookListPresenter<AdminRefBookListPresenter.MyView, AdminRefBookListPresenter.MyProxy> {

    private Map<Integer, String> lstHistory = new HashMap<Integer, String>();
    private Long selectedItemId;

    @Inject
    public AdminRefBookListPresenter(EventBus eventBus, MyView view, MyProxy myProxy, DispatchAsync dispatchAsync) {
        super(eventBus, view, myProxy, dispatchAsync);
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                lstHistory.put(0, lstHistory.get(1));
                lstHistory.put(1, event.getValue());
            }
        });
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        String url = RefBookDataTokens.REFBOOK_SCRIPT + ";" + RefBookDataTokens.REFBOOK_DATA_ID;
        if ((lstHistory.get(0) == null || !lstHistory.get(0).startsWith(url)) &&
                (lstHistory.get(1) == null || !lstHistory.get(1).startsWith(url))) {
            selectedItemId = null;
            getView().setFilter("");
        }
        super.prepareFromRequest(request);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }

    @Override
    protected Long getSelectedId() {
        return selectedItemId;
    }

    @Override
    protected void setSelectedId(Long selectedItemId) {
        this.selectedItemId = selectedItemId;
    }

    @ProxyCodeSplit
    @NameToken(RefBookListTokens.REFBOOK_LIST_ADMIN)
    public interface MyProxy extends ProxyPlace<AdminRefBookListPresenter> {
    }

    public interface MyView extends AbstractRefBookListPresenter.MyView {
    }

    @Override
    protected boolean getOnlyVisible() {
        return false;
    }
}
