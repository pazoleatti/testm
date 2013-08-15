package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefBookDataPresenter extends Presenter<RefBookDataPresenter.MyView,
		RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers  {

	@ProxyCodeSplit
	@NameToken(RefBookDataTokens.refBookData)
	public interface MyProxy extends ProxyPlace<RefBookDataPresenter>, Place {
	}

	private Long refBookDataId;

	private List<RefBookAttribute> refBook;

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	private static final int PAGE_SIZE = 20;
	private final TableDataProvider dataProvider = new TableDataProvider();

	public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
		void setTableColumns(List<RefBookAttribute> headers);
		void setTableData(int start, int totalCount, List<RefBookDataRow> dataRows);
		void createInputFields(List<RefBookAttribute> headers);
		void fillInputFields(Map<String, com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute> data);
		void assignDataProvider(int pageSize, AbstractDataProvider<RefBookDataRow> data);
		void setRange(Range range);
		void updateTable();
	}

	@Inject
	public RefBookDataPresenter(final EventBus eventBus, final MyView view, PlaceManager placeManager, final MyProxy proxy,
	                            DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = (TaPlaceManager)placeManager;
		getView().setUiHandlers(this);
		getView().assignDataProvider(PAGE_SIZE, dataProvider);
	}

	@Override
	public void onCancelClicked() {
		placeManager.revealPlace(new PlaceRequest(RefBookListTokens.refbookList));
	}

	@Override
	public void onAddRowClicked() {

	}

	@Override
	public void onSelectionChanged(Long recordId) {
		GetRefBookDataAction action = new GetRefBookDataAction();
		action.setRefbookId(refBookDataId);
		action.setRecordId(recordId);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookDataResult>() {
							@Override
							public void onSuccess(GetRefBookDataResult result) {
								getView().fillInputFields(result.getRecord());
							}
						}, this));
	}

	@Override
	public void onSaveClicked(Map<String, Object > values) {
		SaveRefBookRowAction action = new SaveRefBookRowAction();
		action.setRefbookId(refBookDataId);
		action.setValueToSave(convertValues(values));
		for(Map.Entry<String, Object> v : values.entrySet()) {
			System.out.println("Val: " + v.getKey() + ": " + v.getValue());
		}
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<SaveRefBookRowResult>() {
							@Override
							public void onSuccess(SaveRefBookRowResult result) {
								getView().updateTable();
							}
						}, this));

	}

	@Override
	public void prepareFromRequest(final PlaceRequest request) {
		super.prepareFromRequest(request);
		refBookDataId = Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null));
		GetRefBookTableDataAction action = new GetRefBookTableDataAction();
		action.setRefbookId(refBookDataId);
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookTableDataResult>() {
							@Override
							public void onSuccess(GetRefBookTableDataResult result) {
								getView().setTableColumns(result.getTableHeaders());
								getView().createInputFields(result.getTableHeaders());
								refBook = result.getTableHeaders();
								getView().setRange(new Range(0, PAGE_SIZE));
							}
						}, this));

	}

	private Map<String, com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute>
		convertValues(Map<String, Object> valuesToConvert) {
		Map<String, com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute> convertedValues =
				new HashMap<String, com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute>();

		for(RefBookAttribute attribute : refBook) {
			com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute refBookAttribute =
					new com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute();
			refBookAttribute.setAttributeType(attribute.getAttributeType());
			String alias = attribute.getAlias();

			switch (attribute.getAttributeType()) {
				case STRING:
					refBookAttribute.setStringValue((String)valuesToConvert.get(alias));
					break;
				case DATE:
					refBookAttribute.setDateValue((Date) valuesToConvert.get(alias));
					break;
				case NUMBER:
					refBookAttribute.setNumberValue((Number) valuesToConvert.get(alias));
					break;
				case REFERENCE:
					refBookAttribute.setReferenceValue((Long)valuesToConvert.get(alias));
					break;
			}

			convertedValues.put(alias, refBookAttribute);
		}
		com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute recordId =
				new com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute();
		recordId.setAttributeType(RefBookAttributeType.NUMBER);
		recordId.setNumberValue((Long)valuesToConvert.get(RefBook.RECORD_ID_ALIAS));
		convertedValues.put(RefBook.RECORD_ID_ALIAS, recordId);

		return convertedValues;
	}

	private class TableDataProvider extends AsyncDataProvider<RefBookDataRow> {

		@Override
		protected void onRangeChanged(HasData<RefBookDataRow> display) {
			if (refBookDataId == null) return;
			final Range range = display.getVisibleRange();
			GetRefBookTableDataAction action = new GetRefBookTableDataAction();
			action.setRefbookId(refBookDataId);
			action.setPagingParams(new PagingParams(range.getStart(), range.getLength()));
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
}
