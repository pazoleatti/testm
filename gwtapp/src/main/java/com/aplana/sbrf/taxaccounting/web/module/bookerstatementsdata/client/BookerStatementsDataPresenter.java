package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client.BookerStatementsTokens;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import com.gwtplatform.mvp.client.proxy.*;

import java.util.*;

/**
 * Presenter для Формы просмотра бухгалтерской отчётности
 *
 * @author lhaziev
 */
public class BookerStatementsDataPresenter extends Presenter<BookerStatementsDataPresenter.MyView,
        BookerStatementsDataPresenter.MyProxy> implements BookerStatementsDataUiHandlers {

    @ProxyCodeSplit
    @NameToken(BookerStatementsDataTokens.bookerStatements)
    public interface MyProxy extends ProxyPlace<BookerStatementsDataPresenter>, Place {
    }

    protected final TaPlaceManager placeManager;

    private final DispatchAsync dispatcher;
    private boolean searchEnabled = false;

    private final TableDataProvider dataProvider = new TableDataProvider();

    private Integer departmentId;
    private Integer reportPeriodId;
    private Integer typeId;

    public interface MyView extends View, HasUiHandlers<BookerStatementsDataUiHandlers> {

        void setAdditionalFormInfo(String department, String reportPeriod, String type);

        void addAccImportValueChangeHandler(ValueChangeHandler<String> valueChangeHandler);

        void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);

        int getPageSize();

        void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> dataProvider);

        void updateTable();

        void setTableColumns(final List<RefBookColumn> columns);

        String getDepartmentName();

        String getReportPeriodName();

        String getBookerReportType();

    }

    @Inject
    public BookerStatementsDataPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy,
                                         DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
        this.dispatcher = dispatcher;
        this.placeManager = (TaPlaceManager)placeManager;
        getView().setUiHandlers(this);
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
    }

    private void onSearch() {
        searchEnabled = true;
        getView().updateTable();
    }

    @Override
    public void onDelete() {
        GetBookerStatementsAction action = new GetBookerStatementsAction();
        action.setDepartmentId(departmentId);
        action.setStatementsKind(typeId);
        action.setReportPeriodId(reportPeriodId);
        action.setNeedOnlyIds(true);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetBookerStatementsResult>() {
            @Override
            public void onSuccess(final GetBookerStatementsResult result) {
                if (result.getUniqueRecordIds() != null && !result.getUniqueRecordIds().isEmpty()) {
                    Dialog.confirmMessage("Вы уверены, что хотите удалить данные бухгалтерской отчётности?",
                            new DialogHandler() {
                                @Override
                                public void yes() {
                                    DeleteBookerStatementsAction action = new DeleteBookerStatementsAction();
                                    action.setStatementsKind(typeId);
                                    action.setUniqueRecordIds(result.getUniqueRecordIds());
                                    dispatcher.execute(action, CallbackUtils
                                            .defaultCallback(new AbstractCallback<DeleteBookerStatementsResult>() {
                                                @Override
                                                public void onSuccess(DeleteBookerStatementsResult result) {
                                                    Dialog.infoMessage("Данные бухгалтерской отчётности (форма " + getView().getBookerReportType() + " для подразделения " + getView().getDepartmentName() + " в периоде " + getView().getReportPeriodName() + ") удалены!");
                                                    onReturnClicked();
                                                }
                                            }, BookerStatementsDataPresenter.this)
                                    );
                                }

                                @Override
                                public void no() {
                                    Dialog.hideMessage();
                                }

                                @Override
                                public void close() {
                                    Dialog.hideMessage();
                                }
                            });
                } else {
                    Dialog.errorMessage("Данные бухгалтерской отчётности (форма " + getView().getBookerReportType() +
                            " для подразделения " + getView().getDepartmentName() +
                            " в периоде " + getView().getReportPeriodName() + ") не существуют!");
                }
            }
        }, BookerStatementsDataPresenter.this));
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);
        if (request.getParameterNames().contains(BookerStatementsDataTokens.DEPARTMENT_ID)) {
            departmentId = Integer.parseInt(request.getParameter(BookerStatementsDataTokens.DEPARTMENT_ID, null));
        }
        if (request.getParameterNames().contains(BookerStatementsDataTokens.REPORT_PERIOD_ID)) {
            reportPeriodId = Integer.parseInt(request.getParameter(BookerStatementsDataTokens.REPORT_PERIOD_ID, null));
        }
        if (request.getParameterNames().contains(BookerStatementsDataTokens.TYPE_ID)) {
            typeId = Integer.parseInt(request.getParameter(BookerStatementsDataTokens.TYPE_ID, null));
        }
        searchEnabled = false;

        GetBSOpenDataAction action = new GetBSOpenDataAction();
        action.setDepartmentId(departmentId);
        action.setReportPeriodId(reportPeriodId);
        action.setStatementsKind(typeId);

        dispatcher.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<GetBSOpenDataResult>() {
                            @Override
                            public void onSuccess(GetBSOpenDataResult result) {
                                if (result == null || result.getControlUNP() == null) {
                                    getProxy().manualRevealFailed();
                                    return;
                                }

                                getView().setAdditionalFormInfo(result.getDepartmentName(), result.getReportPeriodName(), result.getStatementsKindName());
                                onSearch();

                            }
                        }, this).addCallback(new ManualRevealCallback<GetBSOpenDataAction>(this)));
    }

    @Override
    protected void onBind() {
        super.onBind();
        ValueChangeHandler<String> accImportValueChangeHandler = new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                ImportAction importAction = new ImportAction();
                importAction.setUuid(event.getValue());
                importAction.setDepartmentId(departmentId);
                importAction.setReportPeriodId(reportPeriodId);
                importAction.setTypeId(typeId);
                dispatcher.execute(importAction, CallbackUtils.defaultCallback(
                        new AbstractCallback<ImportResult>() {
                            @Override
                            public void onSuccess(ImportResult importResult) {
                                getView().updateTable();
                                Dialog.infoMessage("Загрузка бух отчетности выполнена успешно.");
                            }
                        }, BookerStatementsDataPresenter.this));
            }
        };
        getView().addAccImportValueChangeHandler(accImportValueChangeHandler);
    }

    @Override
    public void onReturnClicked() {
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(BookerStatementsTokens.bookerStatements).build());
    }


    private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {
        @Override
        protected void onRangeChanged(HasData<RefBookDataRow> display) {
            if (searchEnabled) {
                final Range range = display.getVisibleRange();
                GetBookerStatementsAction action = new GetBookerStatementsAction();
                action.setDepartmentId(departmentId);
                action.setStatementsKind(typeId);
                action.setReportPeriodId(reportPeriodId);
                action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
                action.setNeedOnlyIds(false);
                dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetBookerStatementsResult>() {
                    @Override
                    public void onSuccess(GetBookerStatementsResult result) {
                        if (result.isNotBlank()) {
                            getView().setTableColumns(result.getColumns());
                            getView().setTableData(range.getStart(),
                                    result.getTotalCount(), result.getDataRows());
                        } else {
                            searchEnabled = false;
                            Dialog.errorMessage("Невозможно отобразить бухгалтерскую отчетность", "Для выбранного подразделения, в указанном периоде отсутствуют данные по бухгалтерской отчётности!");
                        }
                    }
                }, BookerStatementsDataPresenter.this));
            }
        }
    }
}