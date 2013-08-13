package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.main.api.client.RevealContentTypeHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.TaPlaceManager;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookDataRowAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.GetRefBookDataRowResult;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
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

public class RefBookDataPresenter extends Presenter<RefBookDataPresenter.MyView,
		RefBookDataPresenter.MyProxy> implements RefBookDataUiHandlers  {

	@Override
	public void onCancelClicked() {
		placeManager.revealPlace(new PlaceRequest(RefBookListTokens.refbookList));
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
		GetRefBookDataRowAction action = new GetRefBookDataRowAction();

		action.setRefbookId(Long.parseLong(request.getParameter(RefBookDataTokens.REFBOOK_DATA_ID, null)));
		dispatcher.execute(action,
				CallbackUtils.defaultCallback(
						new AbstractCallback<GetRefBookDataRowResult>() {
							@Override
							public void onSuccess(GetRefBookDataRowResult result) {
								getView().setTableColumns(result.getTableHeaders());
								getView().setTableData(result.getDataRows());
							}
						}, this));
	}
}
