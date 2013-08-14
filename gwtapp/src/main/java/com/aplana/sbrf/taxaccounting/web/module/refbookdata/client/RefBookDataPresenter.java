package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListTokens;
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

import java.util.List;
import java.util.Map;

public class RefBookDataPresenter extends Presenter<RefBookDataPresenter.MyView,
		RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers  {

	Long refBookDataId;

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

	@ProxyCodeSplit
	@NameToken(RefBookDataTokens.refBookData)
	public interface MyProxy extends ProxyPlace<RefBookDataPresenter>, Place {
	}

	private final DispatchAsync dispatcher;
	private final TaPlaceManager placeManager;

	public interface MyView extends View, HasUiHandlers<RefBookDataUiHandlers> {
		void setTableColumns(List<RefBookAttribute> headers);
		void setTableData(List<RefBookDataRow> dataRows);
		void createInputFields(List<RefBookAttribute> headers);
		void fillInputFields(Map<String, com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookAttribute> data);
	}

	@Inject
	public RefBookDataPresenter(final EventBus eventBus, final MyView view, PlaceManager placeManager, final MyProxy proxy,
	                            DispatchAsync dispatcher) {
		super(eventBus, view, proxy, RevealContentTypeHolder.getMainContent());
		this.dispatcher = dispatcher;
		this.placeManager = (TaPlaceManager)placeManager;
		getView().setUiHandlers(this);
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
								getView().setTableData(result.getDataRows());
								getView().createInputFields(result.getTableHeaders());
							}
						}, this));
	}
}
