package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.events.FormDataListApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.events.FormDataListCreateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
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

public class FormDataListPresenter extends
		FormDataListPresenterBase<FormDataListPresenter.MyProxy> implements
		FormDataListUiHandlers, FilterReadyEvent.MyHandler, FormDataListCreateEvent.FormDataCreateHandler,
		FormDataListApplyEvent.FormDataApplyHandler {


	private static final int PAGE_SIZE = 20;
	private static boolean isFirstTime = false;
	private final TableDataProvider dataProvider = new TableDataProvider();

	/**
	 * {@link com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter}
	 * 's proxy.
	 */
	@ProxyCodeSplit
	@NameToken(FormDataListNameTokens.FORM_DATA_LIST)
	public interface MyProxy extends ProxyPlace<FormDataListPresenter>, Place {
	}

	@Inject
	public FormDataListPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			PlaceManager placeManager, DispatchAsync dispatcher,
			FilterPresenter filterPresenter, DialogPresenter dialogPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter, dialogPresenter);
		getView().setUiHandlers(this);
		getView().assignDataProvider(PAGE_SIZE, dataProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gwtplatform.mvp.client.Presenter#prepareFromRequest(com.gwtplatform
	 * .mvp.client.proxy.PlaceRequest)
	 */
	@Override
	public void prepareFromRequest(PlaceRequest request) {
		try {
			LogCleanEvent.fire(this);
			LogShowEvent.fire(this, false);
			super.prepareFromRequest(request);
			TaxType taxType = TaxType.valueOf(request.getParameter("nType", ""));
			filterPresenter.initFilter(taxType);
			getView().updateTitle(taxType.getName());
			dialogPresenter.initDialog(taxType);
		} catch (Exception e) {
			ErrorEvent.fire(this, "Не удалось открыть список форм", e);
		}
	}

	/**
	 * Применение фильтра, обновление списка форм
	 * 
	 * @param filterFormData
	 */
	private void loadFormDataList(final FormDataFilter filterFormData) {
		final int loadDataFromFirstRecord = 0;
		filterFormData.setStartIndex(loadDataFromFirstRecord);
		filterFormData.setCountOfRecords(PAGE_SIZE);

		GetFormDataList action = new GetFormDataList();
		action.setFormDataFilter(filterFormData);
		refreshTable();

		TitleUpdateEvent.fire(this, "Список налоговых форм", filterFormData.getTaxType().getName());
		// Вручную вызывается onReveal. Вызываем его всегда,
		// даже когда
		// презентер в состоянии visible, т.к. нам необходима
		// его разблокировка.
		// Почему GWTP вызывает блокировку даже если страница
		// уже видна - непонятно.
		getProxy().manualReveal(FormDataListPresenter.this);
	}

	@Override
	@ProxyEvent
	public void onFormDataCreateButtonClicked(FormDataListCreateEvent event) {
		dialogPresenter.setSelectedFilterValues(filterPresenter.getFilterData());
		addToPopupSlot(dialogPresenter);
	}

	@Override
	@ProxyEvent
	public void onFormDataApplyButtonClicked(FormDataListApplyEvent event) {
		FormDataFilter filterFormData = filterPresenter.getFilterData();
		loadFormDataList(filterFormData);
		filterPresenter.updateSavedFilterData(filterFormData);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.
	 * FilterReadyEvent
	 * .MyHandler#onFilterReady(com.aplana.sbrf.taxaccounting.web
	 * .module.formdatalist.client.filter.FilterReadyEvent)
	 */
	@ProxyEvent
	@Override
	public void onFilterReady(FilterReadyEvent event) {
		if (event.getSource() == filterPresenter) {
			FormDataFilter filterFormData = filterPresenter.getFilterData();
			loadFormDataList(filterFormData);
		}
	}

	public void refreshTable() {
		dataProvider.update();
	}

	@Override
	public void onSortingChanged(){
		refreshTable();
	}

	private class TableDataProvider extends AsyncDataProvider<FormDataSearchResultItem> {

		private int zeroRecordsCount = 0;

		public void update() {
			for (HasData<FormDataSearchResultItem> display: getDataDisplays()) {
				onRangeChanged(display);
			}
		}

		@Override
		protected void onRangeChanged(HasData<FormDataSearchResultItem> display) {
			final Range range = display.getVisibleRange();
			GetFormDataList requestData = createRequestData(range);
			dispatcher.execute(requestData, CallbackUtils
					.wrongStateCallback(new AbstractCallback<GetFormDataListResult>() {
						@Override
						public void onSuccess(GetFormDataListResult result) {
							if(result == null || result.getTotalCountOfRecords() == zeroRecordsCount){
								getView().setFormDataList(range.getStart(), zeroRecordsCount,
										new ArrayList<FormDataSearchResultItem>());
							} else {
								handleResponse(result, range);
							}
						}
					}, FormDataListPresenter.this));
		}

		private GetFormDataList createRequestData(Range range) {
			// TODO: Данное условие - откровенный костыль, который нужно убирать и реализовать нормально данную функциональность.
			// Зачем нужен это костыль сейчас:
			// когда приложение стартует первый раз, создается SimplePager, который во время инициализации вызывает
			// функцию (onRangeChanged()), которая, в свою очередь, вызывает данную функцию. В данной функции есть строка
			// {@code FormDataFilter filter = filterPresenter.getFilterData(); }, при исполнении которой, в итоге,
			// вызывается {@code driver.flush();}.
			// Все вышеописанное - абсолютно нормальное поведение, но проблема заключается в том, что SimplePager
			// инициализируется раньше чем успевает инициализироваться фильтр, и вызов {@code driver.flush()} раньше
			// чем {@driver.edit()} приводит к зависанию приложения.
			if(isFirstTime){
				FormDataFilter filter = filterPresenter.getFilterData();
				filter.setCountOfRecords(PAGE_SIZE);
				filter.setStartIndex(range.getStart());
				filter.setAscSorting(getView().isAscSorting());
				filter.setSearchOrdering(getView().getSearchOrdering());
				GetFormDataList request = new GetFormDataList();
				request.setFormDataFilter(filter);
				return request;
			}
			isFirstTime = true;
			return (new GetFormDataList());
		}

		private void handleResponse(GetFormDataListResult response, Range range) {
			getView().setFormDataList(range.getStart(), response.getTotalCountOfRecords(), response.getRecords());
		}
	}

}
