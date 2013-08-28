package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.EditFormPresenter;
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
		RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers, UpdateForm.UpdateFormHandler  {

	@ProxyCodeSplit
	@NameToken(RefBookDataTokens.refBookData)
	public interface MyProxy extends ProxyPlace<RefBookDataPresenter>, Place {
	}

	static final Object TYPE_editFormPresenter = new Object();

	private Long refBookDataId;

	EditFormPresenter editFormPresenter;

	private List<RefBookAttribute> refBook;

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	private static final int PAGE_SIZE = 20;
	private final TableDataProvider dataProvider = new TableDataProvider();

	private boolean isValueChanged = false;
	private List<RefBookDataRow> rowsToDelete = new ArrayList<RefBookDataRow>();
	private List<RefBookDataRow> rowsToInsert = new ArrayList<RefBookDataRow>();

	public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
		void setTableColumns(List<RefBookAttribute> headers);
		void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);
		void createInputFields(List<RefBookAttribute> headers);
//		void fillInputFields(Map<String, RefBookAttributeSerializable> data);
		void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data);
		void setRange(Range range);
		void updateTable();
		void addRowToEnd(RefBookDataRow newRow, boolean select);
		Map<String, Object> getChangedValues();
		void setRefBookNameDesc(String desc);
        void resetRefBookElements();
		RefBookDataRow getSelectedRow();
		void clearInputFields();
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
	public void onCancelClicked() {
		rowsToDelete.clear();
		rowsToInsert.clear();
		isValueChanged = false;
		getView().updateTable();
	}

	@Override
	public void onAddRowClicked() {
		getView().clearInputFields();
//		RefBookDataRow newRow = new RefBookDataRow();
//		Map<String, String> newRowData = new HashMap<String, String>();
//		for (RefBookAttribute attribute : refBook) {
//			newRowData.put(attribute.getAlias(), "");
//		}
//		newRow.setValues(newRowData);
//		getView().addRowToEnd(newRow, true);
//		rowsToInsert.add(newRow);

	}

	@Override
	public void onDeleteRowClicked() {
		DeleteRefBookRowAction action = new DeleteRefBookRowAction();
		action.setRefbookId(refBookDataId);
		List<Long> rowsId = new ArrayList<Long>();
		rowsId.add(getView().getSelectedRow().getRefBookRowId());
		action.setRecordsId(rowsId);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<DeleteRefBookRowResult>() {
							@Override
							public void onSuccess(DeleteRefBookRowResult result) {
								getView().updateTable();
							}
						}, this));
	}

	@Override
	public void onSelectionChanged() {
		editFormPresenter.showRecord(refBookDataId, getView().getSelectedRow().getRefBookRowId());
//		GetRefBookDataAction action = new GetRefBookDataAction();
//		action.setRefbookId(refBookDataId);
//		action.setRecordId(getView().getSelectedRow().getRefBookRowId());
//		dispatcher.execute(action,
//				CallbackUtils.defaultCallback(
//						new AbstractCallback<GetRefBookDataResult>() {
//							@Override
//							public void onSuccess(GetRefBookDataResult result) {
//								getView().fillInputFields(result.getRecord());
//							}
//						}, this));
	}

	@Override
	public void onSaveClicked() {
		SaveRefBookRowAction action = new SaveRefBookRowAction();
		action.setRefbookId(refBookDataId);
//		action.setValueToSave(convertValues(getView().getChangedValues()));
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<SaveRefBookRowResult>() {
						@Override
						public void onSuccess(SaveRefBookRowResult result) {
							getView().updateTable();
						}
					}, this));
//		if (!rowsToDelete.isEmpty()) {
//			DeleteRefBookRowAction action = new DeleteRefBookRowAction();
//			action.setRefbookId(refBookDataId);
//			List<Long> rowsId = new ArrayList<Long>();
//			for (RefBookDataRow row : rowsToDelete) {
//				rowsId.add(row.getRefBookRowId());
//			}
//			action.setRecordsId(rowsId);
//			dispatcher.execute(action,
//					CallbackUtils.defaultCallback(
//							new AbstractCallback<DeleteRefBookRowResult>() {
//								@Override
//								public void onSuccess(DeleteRefBookRowResult result) {
//									rowsToDelete.clear();
//									getView().updateTable();
//								}
//							}, this));
//		} else if (!rowsToInsert.isEmpty()) {
//			AddRefBookRowAction action = new AddRefBookRowAction();
//			action.setRefbookId(refBookDataId);
//			List<Map<String, RefBookAttributeSerializable>> toInsert = new ArrayList<Map<String, RefBookAttributeSerializable>>();
//			toInsert.add(convertValues(getView().getChangedValues()));
//			action.setRecords(toInsert);
//			dispatcher.execute(action,
//					CallbackUtils.defaultCallback(
//							new AbstractCallback<AddRefBookRowResult>() {
//								@Override
//								public void onSuccess(AddRefBookRowResult result) {
//									rowsToInsert.clear();
//									getView().updateTable();
//								}
//							}, this));
//		} else if (isValueChanged) {
//			SaveRefBookRowAction action = new SaveRefBookRowAction();
//			action.setRefbookId(refBookDataId);
//
//			action.setValueToSave(convertValues(getView().getChangedValues()));
//			dispatcher.execute(action,
//					CallbackUtils.defaultCallback(
//							new AbstractCallback<SaveRefBookRowResult>() {
//							@Override
//							public void onSuccess(SaveRefBookRowResult result) {
//								getView().updateTable();
//							}
//						}, this));
//		}

	}

	@Override
	public void onValueChanged() {
		isValueChanged = true;
	}

	@Override
	public void prepareFromRequest(final PlaceRequest request) {
		super.prepareFromRequest(request);
		GetRefBookAttributesAction action = new GetRefBookAttributesAction();
		refBookDataId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
		action.setRefbookId(refBookDataId);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookAttributesResult>() {
							@Override
							public void onSuccess(GetRefBookAttributesResult result) {
//                                getView().resetRefBookElements();
								getView().setTableColumns(result.getAttributes());
//								getView().createInputFields(result.getTableHeaders());
//								refBook = result.getTableHeaders();
								getView().setRange(new Range(0, PAGE_SIZE));
//								getView().setRefBookNameDesc(result.getDesc());
								editFormPresenter.init(refBookDataId);
                                getProxy().manualReveal(RefBookDataPresenter.this);
							}
						}, this));

	}

	@Override
	public void onBind(){
		addRegisteredHandler(UpdateForm.getType(), this);
	}

//	private Map<String, RefBookAttributeSerializable> convertValues(Map<String, Object> valuesToConvert) {
//		Map<String, RefBookAttributeSerializable> convertedValues =
//				new HashMap<String, RefBookAttributeSerializable>();
//
//		for(RefBookAttribute attribute : refBook) {
//			RefBookAttributeSerializable refBookAttributeSerializable =
//					new RefBookAttributeSerializable();
//			refBookAttributeSerializable.setAttributeType(attribute.getAttributeType());
//			String alias = attribute.getAlias();
//
//			switch (attribute.getAttributeType()) {
//				case STRING:
//					refBookAttributeSerializable.setStringValue((String)valuesToConvert.get(alias));
//					break;
//				case DATE:
//					refBookAttributeSerializable.setDateValue((Date) valuesToConvert.get(alias));
//					break;
//				case NUMBER:
//					refBookAttributeSerializable.setNumberValue((Number) valuesToConvert.get(alias));
//					break;
//				case REFERENCE:
//					refBookAttributeSerializable.setReferenceValue((Long)valuesToConvert.get(alias));
//					break;
//			}
//
//			convertedValues.put(alias, refBookAttributeSerializable);
//		}
//		RefBookAttributeSerializable recordId =
//				new RefBookAttributeSerializable();
//		recordId.setAttributeType(RefBookAttributeType.NUMBER);
//		recordId.setNumberValue((Long)valuesToConvert.get(RefBook.RECORD_ID_ALIAS));
//		convertedValues.put(RefBook.RECORD_ID_ALIAS, recordId);
//
//		return convertedValues;
//	}

	private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {

		@Override
		protected void onRangeChanged(HasData<RefBookDataRow> display) {
			if (refBookDataId == null) return;
			final Range range = display.getVisibleRange();
			GetRefBookTableDataAction action = new GetRefBookTableDataAction();
			action.setRefbookId(refBookDataId);
			action.setPagingParams(new PagingParams(range.getStart()+1, range.getLength()));
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
