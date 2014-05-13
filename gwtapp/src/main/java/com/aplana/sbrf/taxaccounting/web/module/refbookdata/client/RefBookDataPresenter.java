package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
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
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.*;

public class RefBookDataPresenter extends Presenter<RefBookDataPresenter.MyView,
		RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers,
		UpdateForm.UpdateFormHandler,  RollbackTableRowSelection.RollbackTableRowSelectionHandler{

	@ProxyCodeSplit
	@NameToken(RefBookDataTokens.refBookData)
	public interface MyProxy extends ProxyPlace<RefBookDataPresenter>, Place {
	}

	static final Object TYPE_editFormPresenter = new Object();

	private Long refBookDataId;

    private Long recordId;
    private Integer page;

	EditFormPresenter editFormPresenter;

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	private final TableDataProvider dataProvider = new TableDataProvider();

	public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
		void setTableColumns(final List<RefBookColumn> columns);
		void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);
		void setSelected(Long recordId);
		void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data);
        int getPageSize();
		void setRange(Range range);
		void updateTable();
		void setRefBookNameDesc(String desc);
        void resetRefBookElements();
		RefBookDataRow getSelectedRow();
		Date getRelevanceDate();
        void setReadOnlyMode(boolean readOnly);
        public int getPage();
        public void setPage(int page);
        void clearSelection();
    }

	@Inject
	public RefBookDataPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter, PlaceManager placeManager, final MyProxy proxy,
	                            DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = (TaPlaceManager)placeManager;
		this.editFormPresenter = editFormPresenter;
		getView().setUiHandlers(this);
		getView().assignDataProvider(getView().getPageSize(), dataProvider);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_editFormPresenter);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
        if (recordId != null) getView().setPage(page);
		setInSlot(TYPE_editFormPresenter, editFormPresenter);
	}

	@Override
	public void onUpdateForm(UpdateForm event) {
        if (event.isSuccess()) {
            getView().updateTable();
            editFormPresenter.clearAndDisableForm();
        }
	}

	@Override
	public void onRollbackTableRowSelection(RollbackTableRowSelection event) {
		getView().setSelected(event.getRecordId());
	}

	@Override
	public void onAddRowClicked() {
		editFormPresenter.show(null);
	}

	@Override
	public void onDeleteRowClicked() {
		DeleteRefBookRowAction action = new DeleteRefBookRowAction();
		action.setRefBookId(refBookDataId);
		List<Long> rowsId = new ArrayList<Long>();
		rowsId.add(getView().getSelectedRow().getRefBookRowId());
		action.setRecordsId(rowsId);
        action.setDeleteVersion(false);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<DeleteRefBookRowResult>() {
							@Override
							public void onSuccess(DeleteRefBookRowResult result) {
                                LogCleanEvent.fire(RefBookDataPresenter.this);
                                LogAddEvent.fire(RefBookDataPresenter.this, result.getUuid());
                                if (result.isException()) {
                                    Dialog.errorMessage("Удаление всех версий элемента справочника", "Обнаружены фатальные ошибки!", new DialogHandler() {
                                        @Override
                                        public void close() {
                                            super.close();
                                        }
                                    });
                                }
								editFormPresenter.show(null);
								editFormPresenter.setEnabled(false);
                                getView().clearSelection();
								getView().updateTable();
							}
						}, this));
	}

	@Override
	public void onSelectionChanged() {
		if (getView().getSelectedRow() != null) {
            recordId = getView().getSelectedRow().getRefBookRowId();
            page = getView().getPage();
			editFormPresenter.show(recordId);
            PlaceRequest currentPlaceRequest = placeManager.getCurrentPlaceRequest();
            placeManager.updateHistory(new PlaceRequest.Builder().nameToken(currentPlaceRequest.getNameToken())
                    .with(RefBookDataTokens.REFBOOK_DATA_ID, currentPlaceRequest.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null))
                    .with(RefBookDataTokens.REFBOOK_RECORD_ID, recordId.toString())
                    .build(), true);
        }
	}

	@Override
	public void onRelevanceDateChanged() {
        getView().clearSelection();
		getView().updateTable();
		editFormPresenter.setRelevanceDate(getView().getRelevanceDate());
		editFormPresenter.show(null);
		editFormPresenter.setEnabled(false);
	}

    @Override
    public void onBackClicked() {
        refBookDataId = null;
        recordId = null;
        placeManager.revealPlace(new PlaceRequest.Builder().nameToken(RefBookListTokens.REFBOOK_LIST).build());
    }

	@Override
	public void prepareFromRequest(final PlaceRequest request) {
		super.prepareFromRequest(request);
        editFormPresenter.setVersionMode(false);
        editFormPresenter.setCurrentUniqueRecordId(null);
        editFormPresenter.setRecordId(null);
		GetRefBookAttributesAction action = new GetRefBookAttributesAction();
		refBookDataId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        if (page != null && request.getParameterNames().contains(RefBookDataTokens.REFBOOK_RECORD_ID)) {
            recordId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_RECORD_ID, null));
        } else {
            recordId = null;
            page = null;
        }
		action.setRefBookId(refBookDataId);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookAttributesResult>() {
							@Override
							public void onSuccess(GetRefBookAttributesResult result) {
                                getView().resetRefBookElements();
								getView().setTableColumns(result.getColumns());
								getView().setRange(new Range(0, getView().getPageSize()));
                                getView().setReadOnlyMode(result.isReadOnly());
								editFormPresenter.init(refBookDataId, result.isReadOnly());
                                getProxy().manualReveal(RefBookDataPresenter.this);
							}
						}, this));

		GetNameAction nameAction = new GetNameAction();
		nameAction.setRefBookId(refBookDataId);
		dispatcher.execute(nameAction,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetNameResult>() {
							@Override
							public void onSuccess(GetNameResult result) {
								getView().setRefBookNameDesc(result.getName());
							}
						}, this));

	}

	@Override
	public void onBind(){
		addRegisteredHandler(UpdateForm.getType(), this);
		addRegisteredHandler(RollbackTableRowSelection.getType(), this);
	}

	private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {

		@Override
		protected void onRangeChanged(HasData<RefBookDataRow> display) {
			if (refBookDataId == null) return;
			final Range range = display.getVisibleRange();
			GetRefBookTableDataAction action = new GetRefBookTableDataAction();
			action.setRefBookId(refBookDataId);
			action.setPagingParams(new PagingParams(range.getStart() + 1, range.getLength()));
			action.setRelevanceDate(getView().getRelevanceDate());
			dispatcher.execute(action,
					CallbackUtils.defaultCallback(
							new AbstractCallback<GetRefBookTableDataResult>() {
								@Override
								public void onSuccess(GetRefBookTableDataResult result) {
									getView().setTableData(range.getStart(),
                                            result.getTotalCount(), result.getDataRows());
                                    // http://jira.aplana.com/browse/SBRFACCTAX-5684 автофокус на первую строку
                                    if ((recordId == null || page == null) && !result.getDataRows().isEmpty()) {
                                        getView().setSelected(result.getDataRows().get(0).getRefBookRowId());
                                    }
                                    // http://jira.aplana.com/browse/SBRFACCTAX-5759
                                    if (recordId != null && page != null) {
                                        getView().setSelected(recordId);
                                    }
								}
							}, RefBookDataPresenter.this));
		}
	}

    @Override
    public boolean useManualReveal() {
        return true;
    }
}
