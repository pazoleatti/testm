package com.aplana.sbrf.taxaccounting.web.module.lock.client;

import com.aplana.sbrf.taxaccounting.model.LockDataItem;
import com.aplana.sbrf.taxaccounting.model.LockSearchOrdering;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.ViewWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.module.lock.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.ArrayList;
import java.util.List;

/**
 * Presenter для формы "Планировщик задач"       *
 * @author dloshkarev
 */
public class LockListPresenter extends Presenter<LockListPresenter.MyView,
        LockListPresenter.MyProxy> implements LockListUiHandlers {

    private final DispatchAsync dispatcher;
    private PlaceManager placeManager;

    @ProxyCodeSplit
    @NameToken(LockTokens.lockList)
    public interface MyProxy extends ProxyPlace<LockListPresenter>, Place {
    }

    public interface MyView extends ViewWithSortableTable, HasUiHandlers<LockListUiHandlers> {
        LockSearchOrdering getSearchOrdering();
        List<String> getSelectedItem();
        void updateData(int pageNumber);

        void setTableData(int startIndex, long count, List<LockDataItem> itemList);

        void clearSelection();

        String getFilter();
    }

    @Inject
    public LockListPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                             DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
    }

    @Override
    public void onExtendLock() {
        if (isSelectedTaskExist()) {
            ExtendLockAction action = new ExtendLockAction();
            action.setKeys(getView().getSelectedItem());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<ExtendLockResult>() {
                        @Override
                        public void onSuccess(ExtendLockResult result) {
                            getView().updateData(0);
                        }
                    }, LockListPresenter.this));
        }
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
                        }
                    }, LockListPresenter.this));
        }
    }

    @Override
    public void onFindClicked() {
        getView().updateData(0);
    }

    @Override
    public void onStopAsync() {
        if (isSelectedTaskExist()) {
            StopAsyncAction action = new StopAsyncAction();
            action.setKeys(getView().getSelectedItem());
            dispatcher.execute(action, CallbackUtils
                    .defaultCallback(new AbstractCallback<StopAsyncResult>() {
                        @Override
                        public void onSuccess(StopAsyncResult result) {
                            getView().updateData(0);
                            getView().clearSelection();
                        }
                    }, LockListPresenter.this));
        }
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        getView().updateData(0);
    }

    @Override
    public void onRangeChange(final int start, int length) {
        GetLockListAction action = new GetLockListAction();
        action.setStartIndex(start);
        action.setCountOfRecords(length);
        action.setAscSorting(getView().isAscSorting());
        action.setSearchOrdering(getView().getSearchOrdering());
        action.setFilter(getView().getFilter());
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetLockListResult>() {
                    @Override
                    public void onSuccess(GetLockListResult result) {
                        if (result.getTotalCountOfRecords() == 0)
                            getView().setTableData(start, 0, new ArrayList<LockDataItem>());
                        else
                            getView().setTableData(start, result.getTotalCountOfRecords(), result.getLocks());
                    }
                }, LockListPresenter.this));
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