package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationList;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationListResult;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import java.util.ArrayList;

public class DeclarationListPresenter extends
		DeclarationListPresenterBase<DeclarationListPresenter.MyProxy> implements
		DeclarationListUiHandlers, DeclarationFilterReadyEvent.MyHandler{

	private static final int PAGE_SIZE = 20;
	private static boolean IS_FIRST_TIME = false;
	private final TableDataProvider dataProvider = new TableDataProvider();

	@ProxyCodeSplit
	@NameToken(DeclarationListNameTokens.DECLARATION_LIST)
	public interface MyProxy extends ProxyPlace<DeclarationListPresenter>, Place {
	}

	@Inject
	public DeclarationListPresenter(EventBus eventBus, DeclarationListPresenterBase.MyView view, MyProxy proxy,
	                         PlaceManager placeManager, DispatchAsync dispatcher,
	                         DeclarationFilterPresenter filterPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter);
		getView().setUiHandlers(this);
		getView().assignDataProvider(PAGE_SIZE, dataProvider);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			super.prepareFromRequest(request);
			filterPresenter.initFilter(TaxType.valueOf(request.getParameter(
					"nType", "")));
		} catch (Exception e) {
			ErrorEvent.fire(this, "Не удалось открыть список деклараций", e);
		}
	}

	/**
	 * Применение фильтра, обновление списка деклараций
	 */
	private void loadDeclarationsList() {
		refreshTable();
		updateTitle(filterPresenter.getCurrentTaxType());
		// Вручную вызывается onReveal. Вызываем его всегда,
		// даже когда
		// презентер в состоянии visible, т.к. нам необходима
		// его разблокировка.
		// Почему GWTP вызывает блокировку даже если страница
		// уже видна - непонятно.
		getProxy().manualReveal(DeclarationListPresenter.this);
	}

	@Override
	public void onApplyFilter() {
		loadDeclarationsList();
	}


	@ProxyEvent
	@Override
	public void onFilterReady(DeclarationFilterReadyEvent event) {
		if (event.getSource() == filterPresenter) {
			loadDeclarationsList();
		}
	}

	public void refreshTable() {
		dataProvider.update();
	}

	@Override
	public void onSortingChanged(){
		refreshTable();
	}

	private void updateTitle(TaxType taxType){
		String description = "";
		if(taxType.getName().equals(TaxType.VAT.getName())){
			description = "Деклараци по НДС";
		}  else if (taxType.getName().equals(TaxType.PROPERTY.getName())){
			description = "Деклараци по налогу на имущество";
		}  else if (taxType.getName().equals(TaxType.TRANSPORT.getName())){
			description = "Деклараци по транспортному налогу";
		}  else if (taxType.getName().equals(TaxType.INCOME.getName())){
			description = "Деклараци по налогу на прибыль";
		}
		TitleUpdateEvent.fire(this, "Список деклараций", description);
	}

	private class TableDataProvider extends AsyncDataProvider<DeclarationSearchResultItem> {

		private int ZERO_RECORDS_COUNT = 0;

		public void update() {
			for (HasData<DeclarationSearchResultItem> display: getDataDisplays()) {
				onRangeChanged(display);
			}
		}

		@Override
		protected void onRangeChanged(HasData<DeclarationSearchResultItem> display) {
			final Range range = display.getVisibleRange();
			GetDeclarationList requestData = createRequestData(range);
			dispatcher.execute(requestData,
					new AbstractCallback<GetDeclarationListResult>() {
						@Override
						public void onReqSuccess(GetDeclarationListResult result) {
							if(result == null || result.getTotalCountOfRecords() == ZERO_RECORDS_COUNT){
								getView().setDeclarationsList(range.getStart(), ZERO_RECORDS_COUNT,
										new ArrayList<DeclarationSearchResultItem>());
							} else {
								handleResponse(result, range);
							}
						}
					});
		}

		private GetDeclarationList createRequestData(Range range) {
			if(IS_FIRST_TIME){
				DeclarationFilter filter = filterPresenter.getFilterData();
				filter.setCountOfRecords(PAGE_SIZE);
				filter.setStartIndex(range.getStart());
				filter.setAscSorting(getView().isAscSorting());
				filter.setSearchOrdering(getView().getSearchOrdering());
				GetDeclarationList request = new GetDeclarationList();
				request.setDeclarationFilter(filter);
				return request;
			}
			IS_FIRST_TIME = true;
			return (new GetDeclarationList());
		}

		private void handleResponse(GetDeclarationListResult response, Range range) {
			getView().setDeclarationsList(range.getStart(), response.getTotalCountOfRecords(), response.getRecords());
		}
	}
}
