package com.aplana.sbrf.taxaccounting.web.module.audit.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.DownloadUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.ViewWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.archive.AuditArchiveDialogEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.archive.AuditArchiveDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditClientSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.filter.AuditFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.ManualMenuPresenter;
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
import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditClientPresenter extends Presenter<AuditClientPresenter.MyView, AuditClientPresenter.MyProxy>
        implements AuditClientUIHandler, AuditArchiveDialogEvent.AuditArchiveHandler, AuditClientSearchEvent.MyHandler {

    interface MyView extends ViewWithSortableTable, HasUiHandlers<AuditClientUIHandler> {
        void setAuditTableData(int startIndex, long count, List<LogSearchResultItem> itemList);

        void updateData();

        void updateData(int pageNumber);

        void updateArchiveDateLbl(String archiveDate);

        HistoryBusinessSearchOrdering getSearchOrdering();

        void setVisibleArchiveButton(boolean isVisible);

        void updatePrintReportButtonName(ReportType reportType, boolean isVisible);

        void startTimerReport(ReportType reportType);

        void stopTimerReport(ReportType reportType);

    }

    @ProxyCodeSplit
    @NameToken(AuditToken.AUDIT)
    interface MyProxy extends ProxyPlace<AuditClientPresenter> {
    }

    public static final Object TYPE_AUDIT_FILTER_PRESENTER = new Object();
    protected final DispatchAsync dispatcher;
    private AuditArchiveDialogPresenter auditArchiveDialogPresenter;
    private AuditFilterPresenter auditFilterPresenter;
    private final ManualMenuPresenter manualMenuPresenter;


    @Inject
    public AuditClientPresenter(EventBus eventBus, MyView view, MyProxy proxy, AuditArchiveDialogPresenter auditArchiveDialogPresenter, AuditFilterPresenter auditFilterPresenter, ManualMenuPresenter manualMenuPresenter, DispatchAsync dispatcher) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.auditArchiveDialogPresenter = auditArchiveDialogPresenter;
        this.auditFilterPresenter = auditFilterPresenter;
        this.dispatcher = dispatcher;
        this.manualMenuPresenter = manualMenuPresenter;
        getView().setUiHandlers(this);
    }

    @ProxyEvent
    @Override
    public void onAuditFormSearchButtonClicked(AuditClientSearchEvent event) {
        LogCleanEvent.fire(this);
        LogShowEvent.fire(AuditClientPresenter.this, false);
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
    public void onPrintButtonClicked(final boolean force) {
        try {
            PrintAuditDataAction dataAction = new PrintAuditDataAction();
            dataAction.setLogSystemFilter(new LogSystemAuditFilter(auditFilterPresenter.getLogSystemFilter()));
            dataAction.getLogSystemFilter().setStartIndex(0);
            dataAction.getLogSystemFilter().setCountOfRecords(0);
            dataAction.setForce(force);
            dispatcher.execute(dataAction, CallbackUtils.defaultCallbackNoLock(new AbstractCallback<PrintAuditDataResult>() {
                @Override
                public void onSuccess(PrintAuditDataResult result) {
                    if (result.getUuid() != null) {
                        DownloadUtils.
                                openInIframe("download/downloadBlobController/processLogDownload/" + result.getUuid());
                        getView().updatePrintReportButtonName(ReportType.CSV_AUDIT, false);
                        getView().stopTimerReport(ReportType.CSV_AUDIT);
                        manualMenuPresenter.updateNotificationCount();
                    } else {
                        if (result.isLock()) {
                            Dialog.confirmMessage(result.getRestartMsg(), new DialogHandler() {
                                @Override
                                public void yes() {
                                    onPrintButtonClicked(true);
                                }
                            });
                        } else {
                            if (result.getLogUuid() == null) {
                                MessageEvent.fire(AuditClientPresenter.this, true,
                                        "В журнале аудита отсутствуют записи по заданным параметрам поиска.");
                                return;
                            }
                            LogAddEvent.fire(AuditClientPresenter.this, result.getLogUuid());
                            getView().startTimerReport(ReportType.CSV_AUDIT);
                        }
                    }
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

    @Override
    public void onEventClick(String uuid) {
        LogAddEvent.fire(AuditClientPresenter.this, uuid);
    }

    @Override
    public void onTimerReport(final ReportType reportType,  final boolean isTimer) {
        TimerReportAction action = new TimerReportAction();
        action.setReportType(reportType);
        dispatcher.execute(action, CallbackUtils
                .simpleCallback(new AbstractCallback<TimerReportResult>() {
                    @Override
                    public void onSuccess(TimerReportResult result) {
                        if (reportType==ReportType.ARCHIVE_AUDIT){
                            if (isTimer){
                                GetLastArchiveDateAction action = new GetLastArchiveDateAction();
                                dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(new AbstractCallback<GetLastArchiveDateResult>() {
                                    @Override
                                    public void onSuccess(GetLastArchiveDateResult result) {
                                        getView().updateArchiveDateLbl(result.getLastArchiveDate());
                                    }
                                }, AuditClientPresenter.this));
                                getView().updatePrintReportButtonName(reportType, !result.isLocked() && result.isExist());
                            } else{
                                DownloadUtils.
                                        openInIframe("download/downloadBlobController/processArchiveDownload/" + result.getUuid());
                                getView().stopTimerReport(ReportType.ARCHIVE_AUDIT);
                            }
                        } else if (reportType == ReportType.CSV_AUDIT){
                            if (!isTimer){
                                getView().updatePrintReportButtonName(ReportType.CSV_AUDIT, false);
                                getView().stopTimerReport(ReportType.CSV_AUDIT);
                                manualMenuPresenter.updateNotificationCount();
                            } else{
                                getView().updatePrintReportButtonName(reportType, !result.isLocked() && result.isExist());
                            }
                        }
                    }
                }));
    }

    @Override
    public void downloadArchive() {

    }

    @Override
    public void downloadCsv() {

    }

    @ProxyEvent
    @Override
    public void onAuditArchiveClickEvent(AuditArchiveDialogEvent event) {
        LogSystemFilter logSystemFilter = new LogSystemFilter();
        logSystemFilter.setToSearchDate(event.getArchiveDate());
        AuditArchiveAction action = new AuditArchiveAction();
        action.setLogSystemFilter(logSystemFilter);
        dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(new AbstractCallback<AuditArchiveResult>() {
            @Override
            public void onSuccess(AuditArchiveResult result) {
                if (result.isException()){
                    Dialog.errorMessage("В журнале аудита отсутствуют записи за выбранный период.");
                } else {
                    LogCleanEvent.fire(AuditClientPresenter.this);
                    LogAddEvent.fire(AuditClientPresenter.this, result.getUuid());
                    getView().startTimerReport(ReportType.ARCHIVE_AUDIT);
                }
                /*if (!result.isException()) {
                    //MessageEvent.fire(AuditClientPresenter.this, "Архивация выполнена успешно. Архивировано " + result.getCountOfRemoveRecords() + " записей");
//                    getView().getBlobFromServer(result.getFileUuid());
                    GetLastArchiveDateAction action = new GetLastArchiveDateAction();
                    dispatcher.execute(action, CallbackUtils.defaultCallbackNoLock(new AbstractCallback<GetLastArchiveDateResult>() {
                        @Override
                        public void onSuccess(GetLastArchiveDateResult result) {
                            getView().updateArchiveDateLbl(result.getLastArchiveDate());
                        }
                    }, AuditClientPresenter.this));
                    auditFilterPresenter.initFilterData();
                    auditFilterPresenter.onSearchButtonClicked();
                } else {
                    Dialog.errorMessage("Архивация не выполнена");
                }*/
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
        getView().stopTimerReport(ReportType.CSV_AUDIT);
        getView().stopTimerReport(ReportType.ARCHIVE_AUDIT);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        setInSlot(TYPE_AUDIT_FILTER_PRESENTER, auditFilterPresenter);
        getView().startTimerReport(ReportType.CSV_AUDIT);
        getView().startTimerReport(ReportType.ARCHIVE_AUDIT);
    }
}
