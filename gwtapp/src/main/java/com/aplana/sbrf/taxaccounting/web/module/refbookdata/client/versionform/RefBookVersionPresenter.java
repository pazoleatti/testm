package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.AddItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.DeleteItemEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event.ShowItemEvent;
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
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.ArrayList;
import java.util.List;

public class RefBookVersionPresenter extends PresenterWidget<RefBookVersionPresenter.MyView>
        implements RefBookVersionUiHandlers,
        RollbackTableRowSelection.RollbackTableRowSelectionHandler,
        ILinearRefBookData, DeleteItemEvent.DeleteItemHandler, AddItemEvent.AddItemHandler, UpdateForm.UpdateFormHandler {

	private Long refBookId;
    //Идентификатор записи
    private Long uniqueRecordId;
    //Идентификатор версии
    private Long recordId;
    private boolean isHierarchy = false;
    private Integer selectedRowIndex;

    public void setHierarchy(boolean hierarchy) {
        isHierarchy = hierarchy;
    }

	private final DispatchAsync dispatcher;
	private final PlaceManager placeManager;

    @Override
    public void updateTable() {
        getView().updateTable();
    }

    public Integer getSelectedRowIndex() {
        return getView().getSelectedRowIndex();
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
        getView().updateTable();
    }

    public interface MyView extends View, HasUiHandlers<RefBookVersionUiHandlers> {
		void setTableColumns(final List<RefBookColumn> columns);
		void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);
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
    }

    @Override
    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public void setUniqueRecordId(Long uniqueRecordId) {
        this.uniqueRecordId = uniqueRecordId;
    }

    @Inject
	public RefBookVersionPresenter(final EventBus eventBus, final MyView view,
                                   PlaceManager placeManager, DispatchAsync dispatcher) {
		super(eventBus, view);
		this.dispatcher = dispatcher;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
        TableDataProvider dataProvider = new TableDataProvider();
        getView().assignDataProvider(getView().getPageSize(), dataProvider);
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
        selectedRowIndex = getView().getSelectedRowIndex();
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
                                ShowItemEvent.fire(RefBookVersionPresenter.this, null, null);
                                //editPresenter.clean();
                                if (result.getNextVersion() != null) {
                                    getView().updateTable();
                                } else {
                                    placeManager
                                            .revealPlace(new PlaceRequest.Builder().nameToken(isHierarchy ? RefBookDataTokens.refBookHierData : RefBookDataTokens.refBookData)
                                                    .with(RefBookDataTokens.REFBOOK_DATA_ID, String.valueOf(refBookId))
                                                    .build());
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
            action.setRefBookRecordId(uniqueRecordId);
			action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
			dispatcher.execute(action,
					CallbackUtils.defaultCallback(
							new AbstractCallback<GetRefBookRecordVersionResult>() {
								@Override
								public void onSuccess(GetRefBookRecordVersionResult result) {
									getView().setTableData(range.getStart(),
											result.getTotalCount(), result.getDataRows());
                                    if (recordId == null && !result.getDataRows().isEmpty()) {
                                        getView().setSelected(result.getDataRows().get(0).getRefBookRowId());
                                        // recordCommonId = result.getRefBookRecordCommonId();
                                    } else if (recordId != null){
                                        getView().setSelected(recordId);
                                    }
                                    recordId = null;
                                    if (selectedRowIndex != null && result.getDataRows().size() > selectedRowIndex) {
                                        //сохраняем позицию после удаления записи
                                        getView().setSelected(result.getDataRows().get(selectedRowIndex).getRefBookRowId());
                                    }
                                    selectedRowIndex = null;
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
