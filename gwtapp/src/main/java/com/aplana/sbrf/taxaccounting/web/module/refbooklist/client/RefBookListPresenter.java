package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetTableDataAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.GetTableDataResult;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.LoadRefBookAction;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.LoadRefBookResult;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.TableModel;
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

/**
 * Presenter для формы списка справочников
 *
 * @author Stanislav Yasinskiy
 */
public class RefBookListPresenter extends Presenter<RefBookListPresenter.MyView,
        RefBookListPresenter.MyProxy> implements RefBookListUiHandlers {

    @ProxyCodeSplit
    @NameToken(RefBookListTokens.REFBOOK_LIST)
    public interface MyProxy extends ProxyPlace<RefBookListPresenter>, Place {
    }

    private final DispatchAsync dispatcher;
    private RefBookType filterRefBookType;
    private String filterText;

    public interface MyView extends View, HasUiHandlers<RefBookListUiHandlers> {
        void setTableData(List<TableModel> tableData);
        String getFilter();
    }

    @Inject
    public RefBookListPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        loadData(filterRefBookType, filterText);
    }

    @Override
    public void onFindClicked() {
        filterText = getView().getFilter();
        filterRefBookType = null;
        loadData(filterRefBookType, filterText);
    }

    @Override
    public void onLoadClicked() {
    	LogCleanEvent.fire(this);
        LoadRefBookAction action = new LoadRefBookAction();
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(new AbstractCallback<LoadRefBookResult>() {
					@Override
					public void onSuccess(LoadRefBookResult result) {
						LogAddEvent.fire(RefBookListPresenter.this, result.getEntries());
					}
				}, this));
    }

    private void loadData(RefBookType refBookType, String filter) {
        GetTableDataAction action = new GetTableDataAction();
        action.setType(refBookType);
        action.setFilter(filter);
        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetTableDataResult>() {
                            @Override
                            public void onSuccess(GetTableDataResult result) {
                                List<TableModel> tableData = result.getTableData();
                                if (tableData != null && tableData.size() > 0) {
                                    getView().setTableData(tableData);
                                } else {
                                    getView().setTableData(new ArrayList<TableModel>());
                                }
                                getProxy().manualReveal(RefBookListPresenter.this);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                getProxy().manualRevealFailed();
                            }
                        }, this));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }



}