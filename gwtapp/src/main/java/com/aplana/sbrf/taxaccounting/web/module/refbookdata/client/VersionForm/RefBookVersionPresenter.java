package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.VersionForm;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
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

import java.util.ArrayList;
import java.util.List;

public class RefBookVersionPresenter extends Presenter<RefBookVersionPresenter.MyView,
		RefBookVersionPresenter.MyProxy> implements RefBookVersionUiHandlers,
		UpdateForm.UpdateFormHandler,  RollbackTableRowSelection.RollbackTableRowSelectionHandler{

	@ProxyCodeSplit
	@NameToken(RefBookDataTokens.refBookVersion)
	public interface MyProxy extends ProxyPlace<RefBookVersionPresenter>, Place {
	}

	static final Object TYPE_editFormPresenter = new Object();

	private Long refBookId;
    private Long uniqueRecordId;
    private FormMode mode;

	EditFormPresenter editFormPresenter;

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	private final TableDataProvider dataProvider = new TableDataProvider();

	public interface MyView extends View, HasUiHandlers<RefBookVersionUiHandlers> {
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
        void setTitleDetails(String uniqueAttrValues);
        void setBackAction(String url);
        void updateMode(FormMode mode);
    }

	@Inject
	public RefBookVersionPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter, PlaceManager placeManager, final MyProxy proxy,
                                   DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = (TaPlaceManager)placeManager;
		this.editFormPresenter = editFormPresenter;
		getView().setUiHandlers(this);
		getView().assignDataProvider(getView().getPageSize(), dataProvider);
        mode = FormMode.READ;
        getView().updateMode(mode);
	}

	@Override
	protected void onHide() {
		super.onHide();
		clearSlot(TYPE_editFormPresenter);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		setInSlot(TYPE_editFormPresenter, editFormPresenter);
	}

	@Override
	public void onUpdateForm(UpdateForm event) {
        if (this.isVisible()) {
            getView().updateTable();
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
                                LogCleanEvent.fire(RefBookVersionPresenter.this);
                                LogAddEvent.fire(RefBookVersionPresenter.this, result.getUuid());
								editFormPresenter.show(null);
								editFormPresenter.setMode(mode);
                                if (result.getNextVersion() != null) {
                                    placeManager
                                            .revealPlace(new PlaceRequest.Builder().nameToken(RefBookDataTokens.refBookVersion)
                                                    .with(RefBookDataTokens.REFBOOK_DATA_ID, String.valueOf(refBookId))
                                                    .with(RefBookDataTokens.REFBOOK_RECORD_ID, String.valueOf(result.getNextVersion()))
                                                    .build());
                                } else {
                                    placeManager
                                            .revealPlace(new PlaceRequest.Builder().nameToken(RefBookDataTokens.refBookData)
                                                    .with(RefBookDataTokens.REFBOOK_DATA_ID, String.valueOf(refBookId))
                                                    .build());
                                }
							}
						}, this));
	}

	@Override
	public void onSelectionChanged() {
		if (getView().getSelectedRow() != null) {
            editFormPresenter.show(getView().getSelectedRow().getRefBookRowId());
		}
	}

	@Override
	public void prepareFromRequest(final PlaceRequest request) {
		super.prepareFromRequest(request);
        refBookId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
        uniqueRecordId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_RECORD_ID, null));

        editFormPresenter.setVersionMode(true);
        editFormPresenter.setCurrentUniqueRecordId(null);
        editFormPresenter.setRecordId(null);

        GetRefBookAttributesAction action = new GetRefBookAttributesAction();
		action.setRefBookId(refBookId);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookAttributesResult>() {
							@Override
							public void onSuccess(GetRefBookAttributesResult result) {
                                getView().resetRefBookElements();
								getView().setTableColumns(result.getColumns());
								getView().setRange(new Range(0, getView().getPageSize()));
                                if (result.isReadOnly()){
                                    setMode(FormMode.READ);
                                }
                                editFormPresenter.init(refBookId, result.isReadOnly());
                                getProxy().manualReveal(RefBookVersionPresenter.this);
							}
						}, this));

		GetNameAction nameAction = new GetNameAction();
		nameAction.setRefBookId(refBookId);
        nameAction.setUniqueRecordId(uniqueRecordId);
		dispatcher.execute(nameAction,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetNameResult>() {
							@Override
							public void onSuccess(GetNameResult result) {
								getView().setRefBookNameDesc(result.getName());
                                getView().setTitleDetails(result.getUniqueAttributeValues());
                                String href = "#" + (
                                        result.getRefBookType().equals(RefBookType.LINEAR.getId()) ?
                                        RefBookDataTokens.refBookData :
                                        RefBookDataTokens.refBookHierData
                                ) + ";id=" + refBookId + ";" + RefBookDataTokens.REFBOOK_RECORD_ID + "=" + uniqueRecordId;
                                getView().setBackAction(href);
                                editFormPresenter.setRecordId(result.getRecordId());
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
                                    if (!result.getDataRows().isEmpty()) {
                                        getView().setSelected(result.getDataRows().get(0).getRefBookRowId());
                                    }
								}
							}, RefBookVersionPresenter.this));
		}
	}

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void setMode(FormMode mode) {
        this.mode = mode;
        getView().updateMode(mode);
    }
}
