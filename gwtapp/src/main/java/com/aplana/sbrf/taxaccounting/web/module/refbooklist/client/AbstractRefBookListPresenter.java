package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared.*;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.ArrayList;
import java.util.List;

/**
 * Абстрактный класс презентора списка справочников
 *
 * @author Stanislav Yasinskiy
 * @author Fail Mukhametdinov
 */
public abstract class AbstractRefBookListPresenter<V extends AbstractRefBookListPresenter.MyView, Proxy_ extends ProxyPlace<?>>
        extends Presenter<V, Proxy_> implements RefBookListUiHandlers {

    private final DispatchAsync dispatchAsync;

    public AbstractRefBookListPresenter(EventBus eventBus, V view, Proxy_ proxy, DispatchAsync dispatchAsync) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        super.prepareFromRequest(request);
        onFindClicked();
    }

    @Override
    public void onFindClicked() {
        loadData(getView().getFilter());
    }

    @Override
    public void onLoadClicked() {
        LogCleanEvent.fire(this);
        LoadRefBookAction action = new LoadRefBookAction();
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(new AbstractCallback<LoadRefBookResult>() {
                    @Override
                    public void onSuccess(LoadRefBookResult result) {
                        LogAddEvent.fire(AbstractRefBookListPresenter.this, result.getUuid());
                    }
                }, this));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    /**
     * При загрузки страницы сбрасываем фильтр.
     */
    @Override
    protected void onReveal() {
        super.onReveal();
        LogCleanEvent.fire(this);
    }

    /**
     * Загрузка справочников
     *
     * @param filter фильтр, при поиске
     */
    private void loadData(String filter) {
        GetTableDataAction action = new GetTableDataAction();
        action.setFilter(filter);
        action.setOnlyVisible(getOnlyVisible());
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetTableDataResult>() {
                            @Override
                            public void onSuccess(GetTableDataResult result) {
                                List<TableModel> tableData = result.getTableData();
                                if (tableData != null && !tableData.isEmpty()) {
                                    getView().setTableData(tableData, getSelectedId());
                                } else {
                                    getView().setTableData(new ArrayList<TableModel>(), null);
                                }
                                setSelectedId(null);
                                getProxy().manualReveal(AbstractRefBookListPresenter.this);
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                getProxy().manualRevealFailed();
                            }
                        }, this));
    }

    protected abstract Long getSelectedId();

    protected abstract void setSelectedId(Long selectedId);

    protected boolean getOnlyVisible() {
        return true;
    }

    public interface MyView extends View, HasUiHandlers<RefBookListUiHandlers> {
        void setTableData(List<TableModel> tableData, Long selectedItemId);

        Long getSelectedId();

        String getFilter();

        void setFilter(String filterText);
    }

    @Override
    protected void onHide() {
        super.onHide();
        setSelectedId(getView().getSelectedId());
    }

}
