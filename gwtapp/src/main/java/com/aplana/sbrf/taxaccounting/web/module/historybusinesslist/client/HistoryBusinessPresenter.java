package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client;

import com.aplana.sbrf.taxaccounting.model.LogBusinessSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.event.LogBusinessSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter.HistoryBusinessFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessListAction;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessListResult;
import com.aplana.sbrf.taxaccounting.model.LogBusinessFilterValues;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 */
public class HistoryBusinessPresenter extends Presenter<HistoryBusinessPresenter.MyView,HistoryBusinessPresenter.MyProxy>
        implements LogBusinessSearchEvent.MyHandler, HistoryBusinessUIHandler {

    static final Object TYPE_historyBusinessPresenter = new Object();

    private HistoryBusinessFilterPresenter historyBusinessFilterPresenter;

    protected final DispatchAsync dispatcher;

    @ProxyEvent
    @Override
    public void onLogBusinessSearchButtonClicked(LogBusinessSearchEvent event) {

    }

    @Override
    public void onRangeChange(final int start, int length) {
        GetHistoryBusinessListAction action = new GetHistoryBusinessListAction();
        LogBusinessFilterValues filterValues = historyBusinessFilterPresenter.getLogSystemFilter();
        filterValues.setStartIndex(start);
        filterValues.setCountOfRecords(length);
        action.setFilterValues(filterValues);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetHistoryBusinessListResult>() {
            @Override
            public void onSuccess(GetHistoryBusinessListResult result) {
                if(result==null || result.getTotalCountOfRecords() == 0)
                    getView().setAuditTableData(start, 0, new ArrayList<LogBusinessSearchResultItem>());
                else
                    getView().setAuditTableData(start, result.getTotalCountOfRecords(), result.getRecords());
            }
        }, this));
    }

    @ProxyCodeSplit
    @NameToken(HistoryBusinessToken.HISTORY_BUSINESS)
    interface MyProxy extends ProxyPlace<HistoryBusinessPresenter>{}

    @Inject
    public HistoryBusinessPresenter(EventBus eventBus, MyView view, MyProxy proxy, HistoryBusinessFilterPresenter historyBusinessFilterPresenter, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
        this.historyBusinessFilterPresenter = historyBusinessFilterPresenter;
    }

    interface MyView extends View, HasUiHandlers<HistoryBusinessUIHandler>{
        void setAuditTableData(int startIndex, long count,  List<LogBusinessSearchResultItem> itemList);
        void updateData(int pageNumber);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_historyBusinessPresenter, historyBusinessFilterPresenter);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(TYPE_historyBusinessPresenter);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        historyBusinessFilterPresenter.initFilterData();
    }
}
