package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditClientSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditDataListAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditDataListResult;
import com.google.gwt.view.client.AbstractDataProvider;
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

import java.util.ArrayList;
import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditClientPresenter extends Presenter<AuditClientPresenter.MyView, AuditClientPresenter.MyProxy>
        implements AuditClientSearchEvent.AuditFormSearchHandler {

    @ProxyEvent
    @Override
    public void onAuditFormSearchButtonClicked(AuditClientSearchEvent event) {
        dataProvider.update();

        getProxy().manualReveal(AuditClientPresenter.this);
    }

    interface MyView extends View{
        void setAuditTableData(int startIndex, long count,  List<LogSystemSearchResultItem> itemList);
        void assignDataProvider(int pageSize, AbstractDataProvider<LogSystemSearchResultItem> provider);
    }

    @ProxyCodeSplit
    @NameToken(AuditToken.AUDIT)
    interface MyProxy extends ProxyPlace<AuditClientPresenter> {}

    static final Object TYPE_auditFilterPresenter = new Object();

    private AuditFilterPresenter auditFilterPresenter;

    protected final DispatchAsync dispatcher;

    private MyDataProvider dataProvider = new MyDataProvider();

    private static final int PAGE_SIZE = 30;

    @Inject
    public AuditClientPresenter(EventBus eventBus, MyView view, MyProxy proxy, AuditFilterPresenter auditFilterPresenter, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.auditFilterPresenter = auditFilterPresenter;
        this.dispatcher = dispatcher;
        this.auditFilterPresenter.initFilterData();
        getView().assignDataProvider(PAGE_SIZE, dataProvider);
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


    private class MyDataProvider extends AsyncDataProvider<LogSystemSearchResultItem>{

        private int zeroRecordsCount = 0;

        public void update() {
            for (HasData<LogSystemSearchResultItem> display: getDataDisplays()) {
                onRangeChanged(display);
            }
        }

        @Override
        protected void onRangeChanged(HasData<LogSystemSearchResultItem> display) {
            final Range range = display.getVisibleRange();
            GetAuditDataListAction action = prepareRange(range);
            dispatcher.execute(action, new AbstractCallback<GetAuditDataListResult>() {
                @Override
                public void onSuccess(GetAuditDataListResult result) {
                    if(result==null || result.getTotalCountOfRecords() == zeroRecordsCount)
                        getView().setAuditTableData(range.getStart(), zeroRecordsCount, new ArrayList<LogSystemSearchResultItem>());
                    else
                        getView().setAuditTableData(range.getStart(), result.getTotalCountOfRecords(), result.getRecords());
                }
            });
        }

        private GetAuditDataListAction prepareRange(Range range){
            GetAuditDataListAction action = new GetAuditDataListAction();
            LogSystemFilter filter = auditFilterPresenter.getLogSystemFilter();
            filter.setStartIndex(range.getStart());
            filter.setCountOfRecords(range.getLength());
            action.setLogSystemFilter(filter);

            return action;
        }
    }
}
