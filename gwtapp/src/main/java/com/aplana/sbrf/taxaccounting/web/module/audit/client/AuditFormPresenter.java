package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditFormSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditDataListAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditDataListResult;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFormPresenter extends Presenter<AuditFormPresenter.MyView, AuditFormPresenter.MyProxy>
        implements AuditFormSearchEvent.AuditFormSearchHandler {

    @ProxyEvent
    @Override
    public void onAuditFormSearchButtonClicked(AuditFormSearchEvent event) {
        GetAuditDataListAction action = new GetAuditDataListAction();
        action.setLogSystemFilter(auditFilterPresenter.getLogSystemFilter());
        dispatcher.execute(action, new AbstractCallback<GetAuditDataListResult>() {
            @Override
            public void onSuccess(GetAuditDataListResult result) {
                getView().setAuditTableData(result.getRecords());
            }
        });

        getProxy().manualReveal(AuditFormPresenter.this);
    }

    interface MyView extends View{
        void setAuditTableData(List<LogSystemSearchResultItem> itemList);
    }

    @ProxyCodeSplit
    @NameToken(AuditToken.AUDIT)
    interface MyProxy extends ProxyPlace<AuditFormPresenter> {}

    static final Object TYPE_auditFilterPresenter = new Object();

    private AuditFilterPresenter auditFilterPresenter;

    protected final DispatchAsync dispatcher;

    private static final int PAGE_SIZE = 20;

    @Inject
    public AuditFormPresenter(EventBus eventBus, MyView view, MyProxy proxy, AuditFilterPresenter auditFilterPresenter, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.auditFilterPresenter = auditFilterPresenter;
        this.dispatcher = dispatcher;
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, RevealContentTypeHolder.getMainContent(),
                this);
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

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        auditFilterPresenter.initFilterData();
        GetAuditDataListAction action = new GetAuditDataListAction();
        action.setLogSystemFilter(auditFilterPresenter.getLogSystemFilter());
        dispatcher.execute(action, new AbstractCallback<GetAuditDataListResult>() {
            @Override
            public void onSuccess(GetAuditDataListResult result) {
                getView().setAuditTableData(result.getRecords());
            }
        });
    }

    private static class MyDataProvider extends AsyncDataProvider<LogSystemSearchResultItem>{

        @Override
        protected void onRangeChanged(HasData<LogSystemSearchResultItem> display) {
            final Range range = display.getVisibleRange();
        }
    }
}
