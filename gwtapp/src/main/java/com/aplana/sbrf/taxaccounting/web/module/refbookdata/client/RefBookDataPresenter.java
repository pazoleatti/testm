package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormPresenter;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.RollbackTableRowSelection;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event.UpdateForm;
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

	EditFormPresenter editFormPresenter;

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	private static final int PAGE_SIZE = 20;
	private final TableDataProvider dataProvider = new TableDataProvider();

	public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
		void setTableColumns(List<RefBookAttribute> headers);
		void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);
		void setSelected(Long recordId);
		void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data);
		void setRange(Range range);
		void updateTable();
		void setRefBookNameDesc(String desc);
        void resetRefBookElements();
		RefBookDataRow getSelectedRow();
		Date getRelevanceDate();
    }

	@Inject
	public RefBookDataPresenter(final EventBus eventBus, final MyView view, EditFormPresenter editFormPresenter, PlaceManager placeManager, final MyProxy proxy,
	                            DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = (TaPlaceManager)placeManager;
		this.editFormPresenter = editFormPresenter;
		getView().setUiHandlers(this);
		getView().assignDataProvider(PAGE_SIZE, dataProvider);
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
		getView().updateTable();
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
		action.setRelevanceDate(getView().getRelevanceDate());
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<DeleteRefBookRowResult>() {
							@Override
							public void onSuccess(DeleteRefBookRowResult result) {
								editFormPresenter.show(null);
								editFormPresenter.setEnabled(false);
								getView().updateTable();
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
	public void onRelevanceDateChanged() {
		getView().updateTable();
		editFormPresenter.setRelevanceDate(getView().getRelevanceDate());
		editFormPresenter.show(null);
		editFormPresenter.setEnabled(false);
	}

	@Override
	public void prepareFromRequest(final PlaceRequest request) {
		super.prepareFromRequest(request);
		GetRefBookAttributesAction action = new GetRefBookAttributesAction();
		refBookDataId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
		action.setRefBookId(refBookDataId);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookAttributesResult>() {
							@Override
							public void onSuccess(GetRefBookAttributesResult result) {
                                getView().resetRefBookElements();
								getView().setTableColumns(result.getAttributes());
								getView().setRange(new Range(0, PAGE_SIZE));
//								getView().setRefBookNameDesc(result.getDesc());
								editFormPresenter.init(refBookDataId);
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
								}
							}, RefBookDataPresenter.this));
		}
	}

    @Override
    public boolean useManualReveal() {
        return true;
    }
}
