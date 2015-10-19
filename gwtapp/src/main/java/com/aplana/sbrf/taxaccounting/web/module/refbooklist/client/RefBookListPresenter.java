package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetCurrentUserAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetCurrentUserResult;
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
 * Презентер списка справочников
 *
 * @author Fail Mukhametdinov
 */

public class RefBookListPresenter extends AbstractRefBookListPresenter<RefBookListPresenter.MyView, RefBookListPresenter.MyProxy> {

    private Map<Integer, String> lstHistory = new HashMap<Integer, String>();
    private Long selectedItemId;
    private DispatchAsync dispatchAsync;

    @Inject
    public RefBookListPresenter(EventBus eventBus, MyView view, MyProxy myProxy, DispatchAsync dispatchAsync) {
        super(eventBus, view, myProxy, dispatchAsync);
        this.dispatchAsync = dispatchAsync;
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
        String url = RefBookDataTokens.REFBOOK_HIER_DATA + ";" + RefBookDataTokens.REFBOOK_DATA_ID;
        String url2 = RefBookDataTokens.REFBOOK_DATA + ";" + RefBookDataTokens.REFBOOK_DATA_ID;
        if ((lstHistory.get(0) == null || (!lstHistory.get(0).startsWith(url) && !lstHistory.get(0).startsWith(url2))) &&
                (lstHistory.get(1) == null || (!lstHistory.get(1).startsWith(url) && !lstHistory.get(1).startsWith(url2)))) {
            selectedItemId = null;
            getView().setFilter("");
        }
        GetCurrentUserAction action = new GetCurrentUserAction();
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(new AbstractCallback<GetCurrentUserResult>() {
                    @Override
                    public void onSuccess(GetCurrentUserResult result) {
                        getView().hideLoadButton(result.getUser().hasRole("ROLE_OPER")
                                && (result.getUser().getRoles().size() == 1));
                    }
                }, this));
        super.prepareFromRequest(request);
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
    @NameToken(RefBookListTokens.REFBOOK_LIST)
    public interface MyProxy extends ProxyPlace<RefBookListPresenter> {
    }

    interface MyView extends AbstractRefBookListPresenter.MyView {
        void hideLoadButton(boolean hide);
    }
}