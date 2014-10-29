package com.aplana.sbrf.taxaccounting.web.module.ifrs.client;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.client.create.CreateIfrsDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.model.IfrsRow;
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
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lhaziev on 22.10.2014.
 */
public class IfrsPresenter extends Presenter<IfrsPresenter.MyView, IfrsPresenter.MyProxy> implements IfrsUiHandlers {

    private final DispatchAsync dispatcher;
    private final PlaceManager placeManager;

    protected final CreateIfrsDataPresenter dialogPresenter;

    private final TableDataProvider dataProvider = new TableDataProvider();
    private List<Integer> reportPeriods;

    public interface MyView extends View, HasUiHandlers<IfrsUiHandlers> {
        void setIfrsTableData(int start, int totalCount, List<IfrsRow> records);
        void assignDataProvider(int pageSize, AbstractDataProvider<IfrsRow> data);
        int getPageSize();
        void updateTable();
        void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods);
        List<Integer> getReportPeriodIds();
        Integer getReportPeriodId();
        void startTimer();
        void stopTimer();
    }

    @Title("Отчетность")
    @ProxyCodeSplit
    @NameToken(IfrsTokens.ifrs)
    public interface MyProxy extends ProxyPlace<IfrsPresenter>, Place {
    }

    @Inject
    public IfrsPresenter(EventBus eventBus, MyView view, MyProxy proxy,
                         DispatchAsync dispatcher, PlaceManager placeManager, CreateIfrsDataPresenter dialogPresenter) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        this.dialogPresenter = dialogPresenter;
        getView().setUiHandlers(this);
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
        getView().updateTable();
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);

        getView().startTimer();

        dispatcher.execute(new GetReportPeriodsAction(),
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetReportPeriodsResult>() {
                            @Override
                            public void onSuccess(GetReportPeriodsResult result) {
                                getView().setAcceptableReportPeriods(result.getReportPeriods());
                            }
                        }, IfrsPresenter.this).addCallback(new ManualRevealCallback<GetReportPeriodsResult>(IfrsPresenter.this)));
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().stopTimer();
    }

    @Override
    public void reloadTable() {
        reportPeriods = getView().getReportPeriodIds();
        getView().updateTable();
    }

    @Override
    public void onClickCreate() {
        dialogPresenter.initAndShowDialog(this);
    }


    @Override
    public void onCalc() {
        Integer reportPeriodId = getView().getReportPeriodId();
        if (reportPeriodId != null) {
            CalculateIfrsDataAction action = new CalculateIfrsDataAction();
            action.setReportPeriodId(reportPeriodId);
            LogCleanEvent.fire(this);
            LogShowEvent.fire(this, false);
            dispatcher.execute(action, CallbackUtils.defaultCallback(
                    new AbstractCallback<CalculateIfrsDataResult>() {
                        @Override
                        public void onSuccess(CalculateIfrsDataResult result) {
                            LogAddEvent.fire(IfrsPresenter.this, result.getUuid());
                        }
                    }, this).addCallback(new ManualRevealCallback<CalculateIfrsDataResult>(this)));
        }
    }

    @Override
    public void updateStatus(List<IfrsRow> records) {

    }

    private class TableDataProvider extends AsyncDataProvider<IfrsRow> {

        @Override
        protected void onRangeChanged(HasData<IfrsRow> display) {
            final Range range = display.getVisibleRange();
            GetIfrsDataAction action = new GetIfrsDataAction();
            action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
            action.setReportPeriodIds(reportPeriods);
            dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(
                    new AbstractCallback<GetIrfsDataResult>() {
                        @Override
                        public void onSuccess(GetIrfsDataResult result) {
                            if (result.getTotalCountOfRecords() == 0)
                                getView().setIfrsTableData(range.getStart(), 0, new ArrayList<IfrsRow>());
                            else
                                getView().setIfrsTableData(range.getStart(), result.getIfrsRows().getTotalCount(), result.getIfrsRows());
                        }
                    }, IfrsPresenter.this).addCallback(new ManualRevealCallback<GetIrfsDataResult>(IfrsPresenter.this)));
        }
    }

}
