package com.aplana.sbrf.taxaccounting.web.module.lock.client;

import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataItem;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.*;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
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
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter для формы "Планировщик задач"
 * @author dloshkarev
 */
public class LockListPresenter extends Presenter<LockListPresenter.MyView,
        LockListPresenter.MyProxy> implements LockListUiHandlers {

    private final DispatchAsync dispatcher;
    private final TableDataProvider dataProvider = new TableDataProvider();

    @ProxyCodeSplit
    @NameToken(LockTokens.lockList)
    public interface MyProxy extends ProxyPlace<LockListPresenter>, Place {
    }

    public interface MyView extends View, HasUiHandlers<LockListUiHandlers> {
        List<String> getSelectedItem();
        void updateData(int pageNumber);
        void setTableData(int startIndex, long count, List<LockDataItem> itemList);
        void clearSelection();
        String getFilter();
        int getPageSize();
        void assignDataProvider(int pageSize, AbstractDataProvider<LockDataItem> data);
        void setRoleInfo(int currentUserId, boolean hasRoleAdmin);
        LockData.LockQueues getQueues();
    }

    @Inject
    public LockListPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                             DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
    }

    @Override
    public void onDeleteLock() {
        if (isSelectedTaskExist()) {
            DeleteLockAction action = new DeleteLockAction();
            action.setKeys(getView().getSelectedItem());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<DeleteLockResult>() {
                        @Override
                        public void onSuccess(DeleteLockResult result) {
                            getView().updateData(0);
                            getView().clearSelection();
                            MessageEvent.fire(LockListPresenter.this, "Операция \"Удалить блокировку\" выполнена успешно");
                        }
                    }, LockListPresenter.this));
        }
    }

    @Override
    public void onFindClicked() {
        getView().updateData(0);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        getView().updateData(0);
    }

    private class TableDataProvider extends AsyncDataProvider<LockDataItem> {
        @Override
        protected void onRangeChanged(HasData<LockDataItem> display) {
            final Range range = display.getVisibleRange();
            GetLockListAction action = new GetLockListAction();
            action.setPagingParams(new PagingParams(range.getStart(), range.getLength()));
            action.setFilter(getView().getFilter());
            action.setQueues(getView().getQueues());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<GetLockListResult>() {
                        @Override
                        public void onSuccess(GetLockListResult result) {
                            if (result.getTotalCountOfRecords() == 0) {
                                getView().setTableData(range.getStart(), 0, new ArrayList<LockDataItem>());
                            } else {
                                getView().setTableData(range.getStart(), result.getTotalCountOfRecords(), result.getLocks());
                            }
                            getView().setRoleInfo(result.getCurrentUserId(), result.hasRoleAdmin());
                        }
                    }, LockListPresenter.this));

        }
    }

    @Override
    public void onRangeChange(final int start, int length) {
    }

    /**
     * Проверяет есть ли выбранные задачи
     * @return
     */
    private boolean isSelectedTaskExist() {
        if (getView().getSelectedItem() == null) {
            MessageEvent.fire(this, "Для выполнения этого действия должна быть выбрана одна блокировка из списка");
            return false;
        }
        return true;
    }

}