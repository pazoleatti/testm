package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.ViewWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.archive.AuditArchiveDialogEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.archive.AuditArchiveDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditClientSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditClientPresenter extends Presenter<AuditClientPresenter.MyView, AuditClientPresenter.MyProxy>
        implements AuditClientUIHandler, AuditArchiveDialogEvent.AuditArchiveHandler, AuditClientSearchEvent.MyHandler {

    interface MyView extends ViewWithSortableTable, HasUiHandlers<AuditClientUIHandler> {
        void setAuditTableData(int startIndex, long count, List<LogSearchResultItem> itemList);

        void getBlobFromServer(String uuid);

        void updateData();

        void updateData(int pageNumber);

        void updateArchiveDateLbl(String archiveDate);

        HistoryBusinessSearchOrdering getSearchOrdering();

        void setVisibleArchiveButton(boolean isVisible);

    }

    @ProxyCodeSplit
    @NameToken(AuditToken.AUDIT)
    interface MyProxy extends ProxyPlace<AuditClientPresenter> {
    }

    public static final Object TYPE_AUDIT_FILTER_PRESENTER = new Object();
    protected final DispatchAsync dispatcher;
    private AuditArchiveDialogPresenter auditArchiveDialogPresenter;
    private AuditFilterPresenter auditFilterPresenter;


    @Inject
    public AuditClientPresenter(EventBus eventBus, MyView view, MyProxy proxy, AuditArchiveDialogPresenter auditArchiveDialogPresenter, AuditFilterPresenter auditFilterPresenter, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.auditArchiveDialogPresenter = auditArchiveDialogPresenter;
        this.auditFilterPresenter = auditFilterPresenter;
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @ProxyEvent
    @Override
    public void onAuditFormSearchButtonClicked(AuditClientSearchEvent event) {
        getView().updateData(0);
    }

    @Override
    public void onRangeChange(final int start, int length) {
        GetAuditDataListAction action = new GetAuditDataListAction();
        LogSystemAuditFilter filter = auditFilterPresenter.getLogSystemFilter();
        filter.setStartIndex(start);
        filter.setCountOfRecords(length);
        filter.setAscSorting(getView().isAscSorting());
        filter.setSearchOrdering(getView().getSearchOrdering());
        action.setLogSystemFilter(filter);

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetAuditDataListResult>() {
            @Override
            public void onSuccess(GetAuditDataListResult result) {
                if (result.getTotalCountOfRecords() == 0)
                    getView().setAuditTableData(start, 0, new ArrayList<LogSearchResultItem>());
                else
                    getView().setAuditTableData(start, result.getTotalCountOfRecords(), result.getRecords());
            }
        }, this));
    }

    @Override
    public void onPrintButtonClicked() {
        try {
            PrintAuditDataAction dataAction = new PrintAuditDataAction();
            dataAction.setLogSystemFilter(new LogSystemAuditFilter(auditFilterPresenter.getLogSystemFilter()));
            dataAction.getLogSystemFilter().setStartIndex(0);
            dataAction.getLogSystemFilter().setCountOfRecords(0);
            dispatcher.execute(dataAction, CallbackUtils.defaultCallback(new AbstractCallback<PrintAuditDataResult>() {
                @Override
                public void onSuccess(PrintAuditDataResult result) {
                    getView().getBlobFromServer(result.getUuid());
                }

                @Override
                public void onFailure(Throwable caught) {
                    MessageEvent.fire(AuditClientPresenter.this,
                            "Не удалось напечатать журнал аудита", caught);
                }
            }, this));
        } catch (Exception e) {
            MessageEvent.fire(this,
                    "Не удалось напечатать журнал аудита", e);
        }
    }

    @Override
    public void onArchiveButtonClicked() {
        addToPopupSlot(auditArchiveDialogPresenter);
    }

    @Override
    public void onSortingChanged() {
        getView().updateData();
    }

    @ProxyEvent
    @Override
    public void onAuditArchiveClickEvent(AuditArchiveDialogEvent event) {
        LogSystemFilter logSystemFilter = new LogSystemFilter();
        logSystemFilter.setToSearchDate(event.getArchiveDate());
        logSystemFilter.setFromSearchDate(new Date(0));
        AuditArchiveAction action = new AuditArchiveAction();
        action.setLogSystemFilter(logSystemFilter);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<AuditArchiveResult>() {
            @Override
            public void onSuccess(AuditArchiveResult result) {
                MessageEvent.fire(AuditClientPresenter.this, "Архивация выполнена успешно. Архивировано " + result.getCountOfRemoveRecords() + " записей");
                getView().getBlobFromServer(result.getUuid());
                GetLastArchiveDateAction action = new GetLastArchiveDateAction();
                dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(new AbstractCallback<GetLastArchiveDateResult>() {
                    @Override
                    public void onSuccess(GetLastArchiveDateResult result) {
                        getView().updateArchiveDateLbl(result.getLastArchiveDate());
                    }
                }, AuditClientPresenter.this));
            }
        }, this));
        getProxy().manualReveal(AuditClientPresenter.this);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
                this);
        GetLastArchiveDateAction action = new GetLastArchiveDateAction();
        dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(new AbstractCallback<GetLastArchiveDateResult>() {
            @Override
            public void onSuccess(GetLastArchiveDateResult result) {
                getView().updateArchiveDateLbl(result.getLastArchiveDate());
            }
        }, this));
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        this.auditFilterPresenter.initFilterData();
        getView().updateData(0);
        SetArchiveVisibleAction action = new SetArchiveVisibleAction();
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<SetArchiveVisibleResult>() {
            @Override
            public void onSuccess(SetArchiveVisibleResult result) {
                getView().setVisibleArchiveButton(result.isVisible());
            }
        }, this));
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(TYPE_AUDIT_FILTER_PRESENTER);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_AUDIT_FILTER_PRESENTER, auditFilterPresenter);
    }
}
