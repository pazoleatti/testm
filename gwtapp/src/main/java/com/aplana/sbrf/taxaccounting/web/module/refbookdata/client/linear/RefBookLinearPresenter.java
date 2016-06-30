package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.linear;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.SearchButtonEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.ShowItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.dispatch.shared.DispatchRequest;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;

import java.util.*;

/**
 * User: avanteev
 */
public class RefBookLinearPresenter extends PresenterWidget<RefBookLinearPresenter.MyView>
        implements RefBookDataLinearUiHandlers, ILinearRefBookData,
        RollbackTableRowSelection.RollbackTableRowSelectionHandler,
        DeleteItemEvent.DeleteItemHandler, SearchButtonEvent.SearchHandler, UpdateForm.UpdateFormHandler {

    DispatchAsync dispatchAsync;

    private final TableDataProvider dataProvider = new TableDataProvider();
    private Long refBookDataId;
    private Long recordId;
    private final EditFormPresenter editFormPresenter;
    private DispatchRequest dispatchRequest;

    @Inject
    public RefBookLinearPresenter(EventBus eventBus, EditFormPresenter editFormPresenter, MyView view, DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.editFormPresenter = editFormPresenter;
        this.dispatchAsync = dispatchAsync;
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
        getView().setUiHandlers(this);
    }

    @Override
    public void onSelectionChanged() {
        if (getView().getSelectedRow() != null) {
            recordId = getView().getSelectedRow().getRefBookRowId();
            ShowItemEvent.fire(this, null, recordId);
            /*editPresenter.setRecordId(recordId);
            editPresenter.show(recordId);*/
        } else if (recordId == null) {
            ShowItemEvent.fire(this, null, null);
            //editPresenter.setRecordId(null);
        }
    }

    @Override
    public void onDeleteRowClicked() {
        if (getView().getSelectedRow()==null){
            return;
        }
        DeleteRefBookRowAction action = new DeleteRefBookRowAction();
        action.setRefBookId(refBookDataId);
        List<Long> rowsId = new ArrayList<Long>();
        rowsId.add(getView().getSelectedRow().getRefBookRowId());
        action.setRecordsId(rowsId);
        action.setDeleteVersion(false);
        LogCleanEvent.fire(RefBookLinearPresenter.this);
        RefBookDataRow nextToSelected = getView().getNextToSelected();
        if (nextToSelected != null) {
            recordId = nextToSelected.getRefBookRowId();
        }
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
                                    Dialog.errorMessage("Удаление записи справочника", "Обнаружены фатальные ошибки!");
                                }
                                editFormPresenter.setIsFormModified(false);
                                recordId = null;
                                editFormPresenter.setRecordId(null);
                                editFormPresenter.setCurrentUniqueRecordId(null);

                                /*editPresenter.setMode(mode);*/
                                ShowItemEvent.fire(RefBookLinearPresenter.this, null, recordId);
                                getView().updateTable();
                                /*dataProvider.remove(getSelectedRow());*/
                                /*if (getSelectedRowIndex() != 0) {
                                    getView().setSelected(dataProvider.visibleData.get(dataProvider.visibleData.size() - 1).getRefBookRowId());
                                }*/
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
        dataProvider.exactSearch = event.isExactSearch();
        getView().updateTable();
    }

    @ProxyEvent
    @Override
    public void onUpdateForm(UpdateForm event) {
        /*RefBookDataRow row = new RefBookDataRow();
        row.setValues(event.getRecordChanges().getInfo());
        row.setRefBookRowId(event.getRecordChanges().getId());*/
        recordId = event.getRecordChanges().getId();
        updateTable();
    }

    public interface MyView extends View, HasUiHandlers<RefBookDataLinearUiHandlers> {
        void setTableColumns(final List<RefBookColumn> columns);
        void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows, Long selectedItem);
        void setSelected(Long recordId);
        void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data);
        int getPageSize();
        void setRange(Range range);
        void updateTable();
        void resetRefBookElements();
        RefBookDataRow getSelectedRow();
        int getPage();
        int getPageStart();
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
        int getTotalCount();
        RefBookDataRow getNextToSelected();
    }

    public void setMode(FormMode mode) {
        getView().updateMode(mode);
    }

    @Override
    public void updateTable(){
        getView().updateTable();
    }

    public void initState(Date relevanceDate, String searchPattern, boolean exactSearch) {
        dataProvider.relevanceDate = relevanceDate;
        dataProvider.searchPattern = searchPattern;
        dataProvider.exactSearch = exactSearch;
        /*getView().updateTable();*/
    }

    public RefBookDataRow getSelectedRow() {
        return getView().getSelectedRow();
    }

    public Integer getSelectedRowIndex() {
        return getView().getSelectedRowIndex();
    }

    @Override
    public void setTableColumns(List<RefBookColumn> columns) {
        getView().resetRefBookElements();
        getView().setTableColumns(columns);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setPage(0);
    }

    private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {

        Date relevanceDate;
        String searchPattern;
        Boolean exactSearch;

        @Override
        protected void onRangeChanged(HasData<RefBookDataRow> display) {
            if (refBookDataId == null) return;
            final Range range = display.getVisibleRange();
            GetRefBookTableDataAction action = new GetRefBookTableDataAction();
            action.setRecordId(recordId);
            final Long refBookId = refBookDataId;
            action.setRefBookId(refBookId);
            action.setPagingParams(new PagingParams(range.getStart()+1, range.getLength()));
            action.setRelevanceDate(relevanceDate);
            action.setSearchPattern(searchPattern);
            action.setExactSearch(exactSearch);
            action.setSortColumnIndex(getView().getSortColumnIndex());
            action.setAscSorting(getView().isAscSorting());
            dispatchRequest = dispatchAsync.execute(action,
                    CallbackUtils.defaultCallbackNoLock(
                            new AbstractCallback<GetRefBookTableDataResult>() {
                                @Override
                                public void onSuccess(GetRefBookTableDataResult result) {
                                    if (refBookDataId != null && refBookDataId.equals(refBookId)) {
                                        getView().setTableData(range.getStart(),
                                                result.getTotalCount(), result.getDataRows(), recordId);
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
        addVisibleHandler(RollbackTableRowSelection.getType(), this);
        addVisibleHandler(DeleteItemEvent.getType(), this);
        addVisibleHandler(SearchButtonEvent.getType(), this);
        addVisibleHandler(UpdateForm.getType(), this);
    }

    public Date getRelevanceDate() {
        return dataProvider.relevanceDate;
    }

    public String getSearchPattern() {
        return dataProvider.searchPattern;
    }

    public Boolean getExactSearch() {
        return dataProvider.exactSearch;
    }

    public int getSortColumnIndex() {
        return getView().getSortColumnIndex();
    }

    public boolean isAscSorting() {
        return getView().isAscSorting();
    }

    public void reset() {
        recordId = null;
        refBookDataId = null;
        if (dispatchRequest != null && dispatchRequest.isPending()) {
            dispatchRequest.cancel();
            dispatchRequest = null;
        }
    }

    @Override
    protected void onHide() {
        super.onHide();
        if (dispatchRequest != null && dispatchRequest.isPending()) {
            dispatchRequest.cancel();
            dispatchRequest = null;
        }
    }
}
