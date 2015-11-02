package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.AddItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.ShowItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.event.BackEvent;
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
import java.util.List;

public class RefBookVersionPresenter extends PresenterWidget<RefBookVersionPresenter.MyView>
        implements RefBookVersionUiHandlers,
        RollbackTableRowSelection.RollbackTableRowSelectionHandler,
        ILinearRefBookData, DeleteItemEvent.DeleteItemHandler, AddItemEvent.AddItemHandler, UpdateForm.UpdateFormHandler {

	private Long refBookId;
    //Идентификатор записи
    private Long uniqueRecordId, recordId;
    private boolean isHierarchy = false;

    public void setHierarchy(boolean hierarchy) {
        isHierarchy = hierarchy;
    }

	private final DispatchAsync dispatcher;

    @Override
    public void updateTable() {
        getView().updateTable();
    }

    public RefBookDataRow getSelectedRow() {
        return getView().getSelectedRow();
    }

    @Override
    public void setTableColumns(List<RefBookColumn> columns) {
        getView().resetRefBookElements();
        getView().setTableColumns(columns);
    }

    @ProxyEvent
    @Override
    public void onDeleteItem(DeleteItemEvent event) {
        getView().deleteRowButtonClicked();
    }

    @ProxyEvent
    @Override
    public void onAddItem(AddItemEvent event) {
        onAddRowClicked();
    }

    @ProxyEvent
    @Override
    public void onUpdateForm(UpdateForm event) {
        uniqueRecordId = event.getRecordChanges().getId();
        getView().updateTable();
    }

    public interface MyView extends View, HasUiHandlers<RefBookVersionUiHandlers> {
		void setTableColumns(final List<RefBookColumn> columns);
		void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows, Long selectedItem);
		void setSelected(Long recordId);
		void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data);
        int getPageSize();
		void setRange(Range range);
		void updateTable();
        void resetRefBookElements();
		RefBookDataRow getSelectedRow();

        void updateMode(FormMode mode);
        // позиция выделенной строки в таблице
        Integer getSelectedRowIndex();

        void deleteRowButtonClicked();
        void setPage(int page);
        int getPageStart();
        int getTotalCount();
    }

    @Override
    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public void setUniqueRecordId(Long uniqueRecordId) {
        this.uniqueRecordId = uniqueRecordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    @Inject
	public RefBookVersionPresenter(final EventBus eventBus, final MyView view,
                                   DispatchAsync dispatcher) {
		super(eventBus, view);
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
        getView().assignDataProvider(getView().getPageSize(), new TableDataProvider());
        /*setMode(FormMode.READ);*/
	}

	@Override
	protected void onHide() {
		super.onHide();
	}

	@Override
	protected void onReveal() {
		super.onReveal();
	}

	@Override
	public void onRollbackTableRowSelection(RollbackTableRowSelection event) {
		getView().setSelected(event.getRecordId());
	}

	private void onAddRowClicked() {
        //getView().updateMode(FormMode.CREATE);
        //editPresenter.setMode(FormMode.CREATE);
        if (isHierarchy){
            //http://jira.aplana.com/browse/SBRFACCTAX-10062
            GetLastVersionHierarchyAction action
                    = new GetLastVersionHierarchyAction();
            action.setRefBookId(refBookId);
            action.setRefBookRecordId(uniqueRecordId);
            dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetLastVersionHierarchyResult>() {
                @Override
                public void onSuccess(GetLastVersionHierarchyResult result) {
                    ShowItemEvent.fire(RefBookVersionPresenter.this, result.getDataRow().getValues().get("PARENT_ID"),result.getDataRow().getRefBookRowId());
                    //editPresenter.show(result.getDataRow().getRefBookRowId());
                }
            }, this));
        }
        else{
            ShowItemEvent.fire(RefBookVersionPresenter.this, null, null);
            //editPresenter.clean();
        }
	}

	@Override
	public void onDeleteRowClicked() {
		DeleteRefBookRowAction action = new DeleteRefBookRowAction();
		action.setRefBookId(refBookId);
		List<Long> rowsId = new ArrayList<Long>();
        Long deletedVersion = getView().getSelectedRow().getRefBookRowId();
		rowsId.add(deletedVersion);
		action.setRecordsId(rowsId);
        action.setDeleteVersion(true);
		dispatcher.execute(action,
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
                                LogCleanEvent.fire(RefBookVersionPresenter.this);
                                LogAddEvent.fire(RefBookVersionPresenter.this, result.getUuid());
//                                ShowItemEvent.fire(RefBookVersionPresenter.this, null, null);
                                //editPresenter.clean();
                                if (result.getNextVersion() != null) {
                                    setUniqueRecordId(result.getNextVersion());
                                    getView().setSelected(result.getNextVersion());
                                    RefBookDataRow row = getView().getSelectedRow();
                                    ShowItemEvent.fire(
                                            RefBookVersionPresenter.this, isHierarchy ? row.getValues().get("PARENT_ID") : null,
                                            result.getNextVersion());
                                    getView().updateTable();
                                } else {
                                    /*placeManager
                                            .revealPlace(new PlaceRequest.Builder().nameToken(isHierarchy ? RefBookDataTokens.REFBOOK_HIER_DATA : RefBookDataTokens.REFBOOK_DATA)
                                                    .with(RefBookDataTokens.REFBOOK_DATA_ID, String.valueOf(refBookId))
                                                    .build());*/
                                    BackEvent.fire(RefBookVersionPresenter.this);
                                    ShowItemEvent.fire(RefBookVersionPresenter.this, null, null);
                                }
							}
						}, this));
	}

	@Override
	public void onSelectionChanged() {
		if (getView().getSelectedRow() != null) {
            ShowItemEvent.fire(this, null, getView().getSelectedRow().getRefBookRowId());
            //editPresenter.show(getView().getSelectedRow().getRefBookRowId());
        }
	}

	@Override
	public void onBind(){
		addRegisteredHandler(RollbackTableRowSelection.getType(), this);
        addVisibleHandler(DeleteItemEvent.getType(), this);
        addVisibleHandler(AddItemEvent.getType(), this);
        addVisibleHandler(UpdateForm.getType(), this);
	}

	private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {

		@Override
		protected void onRangeChanged(HasData<RefBookDataRow> display) {
			if (refBookId == null) return;
			final Range range = display.getVisibleRange();
            GetRefBookRecordVersionAction action = new GetRefBookRecordVersionAction();
            action.setRefBookId(refBookId);
            action.setRefBookRecordId(recordId);
			action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
			dispatcher.execute(action,
					CallbackUtils.defaultCallback(
							new AbstractCallback<GetRefBookRecordVersionResult>() {
								@Override
								public void onSuccess(GetRefBookRecordVersionResult result) {
									getView().setTableData(range.getStart(),
											result.getTotalCount(), result.getDataRows(), uniqueRecordId);
								}
							}, RefBookVersionPresenter.this));
		}
	}

    @Override
    public void setMode(FormMode mode) {
        getView().updateMode(mode);
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().setPage(0);
    }
}
