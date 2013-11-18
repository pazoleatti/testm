package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.archive.AuditArchiveDialogEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.archive.AuditArchiveDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditClientArchiveEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditClientSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPrintEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.*;
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
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditClientPresenter extends Presenter<AuditClientPresenter.MyView, AuditClientPresenter.MyProxy>
        implements AuditClientUIHandler,
        AuditClientSearchEvent.MyHandler, AuditClientArchiveEvent.AuditClientArchiveHandler,
        AuditArchiveDialogEvent.AuditArchiveHandler, AuditFilterPrintEvent.AuditFilterPrintHandler {

    private AuditArchiveDialogPresenter auditArchiveDialogPresenter;

    @ProxyEvent
    @Override
    public void onAuditFormSearchButtonClicked(AuditClientSearchEvent event) {
        getView().updateData(0);
    }

    @ProxyEvent
    @Override
    public void onAuditArchiveButtonClick(AuditClientArchiveEvent event) {
        addToPopupSlot(auditArchiveDialogPresenter);
    }

    @Override
    public void onRangeChange(final int start, int length) {
        GetAuditDataListAction action = new GetAuditDataListAction();
        LogSystemFilter filter = auditFilterPresenter.getLogSystemFilter();
        filter.setStartIndex(start);
        filter.setCountOfRecords(length);
        action.setLogSystemFilter(filter);

        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetAuditDataListResult>() {
            @Override
            public void onSuccess(GetAuditDataListResult result) {
                if(result==null || result.getTotalCountOfRecords() == 0)
                    getView().setAuditTableData(start, 0, new ArrayList<LogSearchResultItem>());
                else
                    getView().setAuditTableData(start, result.getTotalCountOfRecords(), result.getRecords());
            }
        }, this));
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
                MessageEvent.fire(AuditClientPresenter.this, "Архивация выполнена успешно.");
                getView().getBlobFromServer(result.getUuid());
            }
        }, this));
        getProxy().manualReveal(AuditClientPresenter.this);
    }

    @ProxyEvent
    @Override
    public void onPrintButtonClicked(AuditFilterPrintEvent event) {
        try{
            PrintAuditDataAction dataAction = new PrintAuditDataAction();
            dataAction.setLogSystemFilter(auditFilterPresenter.getLogSystemFilter());
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
        }catch (Exception e){
            MessageEvent.fire(this,
                    "Не удалось напечатать журнал аудита", e);
        }
    }

    interface MyView extends View,HasUiHandlers<AuditClientUIHandler> {
        void setAuditTableData(int startIndex, long count,  List<LogSearchResultItem> itemList);
        void getBlobFromServer(String uuid);
        void updateData(int pageNumber);
    }

    @ProxyCodeSplit
    @NameToken(AuditToken.AUDIT)
    interface MyProxy extends ProxyPlace<AuditClientPresenter> {}

    static final Object TYPE_auditFilterPresenter = new Object();

    private AuditFilterPresenter auditFilterPresenter;

    protected final DispatchAsync dispatcher;


    @Inject
    public AuditClientPresenter(EventBus eventBus, MyView view, MyProxy proxy, AuditArchiveDialogPresenter auditArchiveDialogPresenter, AuditFilterPresenter auditFilterPresenter, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.auditArchiveDialogPresenter = auditArchiveDialogPresenter;
        this.auditFilterPresenter = auditFilterPresenter;
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
                this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        this.auditFilterPresenter.initFilterData();
        getView().updateData(0);
    }

    @Override
    protected void onHide() {
        super.onHide();
        clearSlot(TYPE_auditFilterPresenter);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_auditFilterPresenter, auditFilterPresenter);
    }

}
