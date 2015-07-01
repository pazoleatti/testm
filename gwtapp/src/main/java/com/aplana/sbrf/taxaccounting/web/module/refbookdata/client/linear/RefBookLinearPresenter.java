package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.SearchButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class RefBookLinearPresenter extends PresenterWidget<RefBookLinearPresenter.MyView>
        implements RefBookDataLinearUiHandlers, ILinearRefBookData,
        RollbackTableRowSelection.RollbackTableRowSelectionHandler, DeleteItemEvent.DeleteItemHandler, SearchButtonEvent.SearchHandler {

    DispatchAsync dispatchAsync;

    EditFormPresenter editFormPresenter;

    private final TableDataProvider dataProvider = new TableDataProvider();
    private Long refBookDataId;
    private Long recordId;

    @Inject
    public RefBookLinearPresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync, EditFormPresenter editFormPresenter) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        this.editFormPresenter = editFormPresenter;
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
        getView().setUiHandlers(this);
    }

    @Override
    public void onSelectionChanged() {
        if (getView().getSelectedRow() != null) {
            recordId = getView().getSelectedRow().getRefBookRowId();
            editFormPresenter.setRecordId(recordId);
            editFormPresenter.show(recordId);
        } else {
            editFormPresenter.setRecordId(null);
        }
    }

    @Override
    public void onDeleteRowClicked() {
        DeleteRefBookRowAction action = new DeleteRefBookRowAction();
        action.setRefBookId(refBookDataId);
        List<Long> rowsId = new ArrayList<Long>();
        rowsId.add(getView().getSelectedRow().getRefBookRowId());
        action.setRecordsId(rowsId);
        action.setDeleteVersion(false);
        LogCleanEvent.fire(RefBookLinearPresenter.this);
        dispatchAsync.execute(action,
                CallbackUtils.defaultCallback(
                        new AbstractCallback<DeleteRefBookRowResult>() {
                            @Override
                            public void onSuccess(DeleteRefBookRowResult result) {
                                if (!result.isCheckRegion()) {
                                    String title = "Удаление элемента справочника";
                                    String msg = "Отсутствуют права доступа на удаление записи для указанного региона!";
                                    Dialog.errorMessage(title, msg);
                                    return;
                                }
                                LogAddEvent.fire(RefBookLinearPresenter.this, result.getUuid());
                                if (result.isException()) {
                                    Dialog.errorMessage("Удаление всех версий элемента справочника", "Обнаружены фатальные ошибки!");
                                }
                                /*editFormPresenter.setMode(mode);*/
                                editFormPresenter.show(null);
                                getView().updateTable();
                            }
                        }, this));
    }

    @Override
    public void onRollbackTableRowSelection(RollbackTableRowSelection event) {
        getView().setSelected(event.getRecordId());
    }

    @ProxyEvent
    @Override
    public void onDeleteItem(DeleteItemEvent event) {
        getView().deleteRowButtonClicked();
    }

    @ProxyEvent
    @Override
    public void onSearch(SearchButtonEvent event) {
        dataProvider.relevanceDate = event.getRelevanceDate();
        dataProvider.searchPattern = event.getSearchPattern();
        getView().updateTable();
    }

    public interface MyView extends View, HasUiHandlers<RefBookDataLinearUiHandlers> {
        void setTableColumns(final List<RefBookColumn> columns);
        void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);
        void setSelected(Long recordId);
        void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data);
        int getPageSize();
        void setRange(Range range);
        void updateTable();
        void resetRefBookElements();
        RefBookDataRow getSelectedRow();
        int getPage();
        void setPage(int page);
        /** Обновление вьюшки для определенного состояния */
        void updateMode(FormMode mode);
        // Номер столбца, по которому осуществляется сортировка
        int getSortColumnIndex();
        // Признак сортировки по-возрастанию
        boolean isAscSorting();
        // позиция выделенной строки в таблице
        Integer getSelectedRowIndex();
        void setEnable(FormMode mode);
        void deleteRowButtonClicked();
    }

    public void setMode(FormMode mode) {
        getView().updateMode(mode);
    }

    @Override
    public void updateTable(){
        getView().updateTable();
    }

    public void initState(Date relevanceDate, String searchPattern) {
        dataProvider.relevanceDate = relevanceDate;
        dataProvider.searchPattern = searchPattern;
        /*getView().updateTable();*/
    }

    @Override
    public RefBookDataRow getSelectedRow() {
        return getView().getSelectedRow();
    }

    @Override
    public Integer getSelectedRowIndex() {
        return getView().getSelectedRowIndex();
    }

    @Override
    public void setTableColumns(List<RefBookColumn> columns) {
        getView().resetRefBookElements();
        getView().setTableColumns(columns);
    }

    @Override
    public void setRange(Range range) {
        getView().setRange(range);
    }

    @Override
    public int getPageSize() {
        return getView().getPageSize();
    }

    @Override
    public void blockDataView(FormMode mode) {
        getView().updateMode(mode);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }

    private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {

        Date relevanceDate;
        String searchPattern;

        @Override
        protected void onRangeChanged(HasData<RefBookDataRow> display) {
            if (refBookDataId == null) return;
            final Range range = display.getVisibleRange();
            GetRefBookTableDataAction action = new GetRefBookTableDataAction();
            action.setRecordId(recordId);
            action.setRefBookId(refBookDataId);
            action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
            action.setRelevanceDate(relevanceDate);
            action.setSearchPattern(searchPattern);
            action.setSortColumnIndex(getView().getSortColumnIndex());
            action.setAscSorting(getView().isAscSorting());
            dispatchAsync.execute(action,
                    CallbackUtils.defaultCallbackNoLock(
                            new AbstractCallback<GetRefBookTableDataResult>() {
                                @Override
                                public void onSuccess(GetRefBookTableDataResult result) {
                                    if (result.getRowNum() != null) {
                                        int page = (int) ((result.getRowNum() - 1) / range.getLength());
                                        if (page != getView().getPage()) {
                                            getView().setPage(page);
                                            return;
                                        }
                                    }
                                    getView().setTableData(range.getStart(),
                                            result.getTotalCount(), result.getDataRows());
                                    // http://jira.aplana.com/browse/SBRFACCTAX-5684 автофокус на первую строку
                                    if (!result.getDataRows().isEmpty()) {
                                        getView().setSelected(result.getDataRows().get(0).getRefBookRowId());
                                    } else {
                                        editFormPresenter.cleanFields();
                                        editFormPresenter.clearRecordId();
                                        getView().setSelected(recordId);
                                    }
                                    // http://jira.aplana.com/browse/SBRFACCTAX-5759
                                    /*if (recordId != null) {
                                        getView().setSelected(recordId);
                                    }
                                    recordId = null;*/
                                    /*if (selectedRowIndex != null && result.getDataRows().size() > selectedRowIndex) {
                                        //сохраняем позицию после удаления записи
                                        getView().setSelected(result.getDataRows().get(selectedRowIndex).getRefBookRowId());
                                    }
                                    selectedRowIndex = null;*/
                                    if (result.getDataRows().size() == 0) {
                                        editFormPresenter.setAllVersionVisible(false);
                                    }
                                }
                            }, RefBookLinearPresenter.this));
        }
    }

    @Override
    public void setRefBookId(Long refBookDataId) {
        this.refBookDataId = refBookDataId;
    }

    @Override
    protected void onBind() {
        super.onBind();
        addRegisteredHandler(RollbackTableRowSelection.getType(), this);
        addVisibleHandler(DeleteItemEvent.getType(), this);
        addVisibleHandler(SearchButtonEvent.getType(), this);
    }
}
