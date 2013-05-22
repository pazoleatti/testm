package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation.DeclarationCreationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.google.gwt.view.client.*;
import com.google.inject.*;
import com.google.web.bindery.event.shared.*;
import com.gwtplatform.dispatch.shared.*;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.*;

import java.util.*;

public class DeclarationListPresenter extends
		DeclarationListPresenterBase<DeclarationListPresenter.MyProxy> implements
		DeclarationListUiHandlers, DeclarationFilterReadyEvent.MyHandler{

	private static final int PAGE_SIZE = 20;
	private static boolean isFirstTime = false;
	private final TableDataProvider dataProvider = new TableDataProvider();

	@ProxyCodeSplit
	@NameToken(DeclarationListNameTokens.DECLARATION_LIST)
	public interface MyProxy extends ProxyPlace<DeclarationListPresenter>, Place {
	}

	@Inject
	public DeclarationListPresenter(EventBus eventBus, DeclarationListPresenterBase.MyView view, MyProxy proxy,
	                         PlaceManager placeManager, DispatchAsync dispatcher,
	                         DeclarationFilterPresenter filterPresenter, DeclarationCreationPresenter creationPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter, creationPresenter);
		getView().setUiHandlers(this);
		getView().assignDataProvider(PAGE_SIZE, dataProvider);
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			LogCleanEvent.fire(this);
			LogShowEvent.fire(this, false);
			super.prepareFromRequest(request);
			TaxType taxType = TaxType.valueOf(request.getParameter("nType", ""));
			filterPresenter.initFilter(taxType);
			getView().updateTitle(taxType.getName());
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
		filterPresenter.updateSavedFilterData(filterPresenter.getFilterData());
	}

	@Override
	public void onCreateClicked() {
		creationPresenter.setDeclarationFilter(filterPresenter.getFilterData());
		creationPresenter.setFilterValues(filterPresenter.getFilterValues());
		creationPresenter.setTaxPeriods(filterPresenter.getTaxPeriods());
		creationPresenter.setDepartments(filterPresenter.getDepartments());
		creationPresenter.setCurrentReportPeriod(filterPresenter.getCurrentReportPeriod());
		addToPopupSlot(creationPresenter);
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

	private class TableDataProvider extends AsyncDataProvider<DeclarationDataSearchResultItem> {

		private int zeroRecordsCount = 0;

		public void update() {
			for (HasData<DeclarationDataSearchResultItem> display: getDataDisplays()) {
				onRangeChanged(display);
			}
		}

		@Override
		protected void onRangeChanged(HasData<DeclarationDataSearchResultItem> display) {
			final Range range = display.getVisibleRange();
			GetDeclarationList requestData = createRequestData(range);
			dispatcher.execute(requestData, CallbackUtils
					.defaultCallback(new AbstractCallback<GetDeclarationListResult>() {
						@Override
						public void onSuccess(GetDeclarationListResult result) {
							if(result == null || result.getTotalCountOfRecords() == zeroRecordsCount){
								getView().setDeclarationsList(range.getStart(), zeroRecordsCount,
										new ArrayList<DeclarationDataSearchResultItem>());
							} else {
								handleResponse(result, range);
							}
						}
					}, DeclarationListPresenter.this));
		}

		private GetDeclarationList createRequestData(Range range) {
			if(isFirstTime){
				DeclarationDataFilter filter = filterPresenter.getFilterData();
				filter.setCountOfRecords(PAGE_SIZE);
				filter.setStartIndex(range.getStart());
				filter.setAscSorting(getView().isAscSorting());
				filter.setSearchOrdering(getView().getSearchOrdering());

				GetDeclarationList request = new GetDeclarationList();
				request.setDeclarationFilter(filter);
				return request;
			}
			isFirstTime = true;
			return (new GetDeclarationList());
		}

		private void handleResponse(GetDeclarationListResult response, Range range) {
			getView().setDeclarationsList(range.getStart(), response.getTotalCountOfRecords(), response.getRecords());
		}
	}
}
