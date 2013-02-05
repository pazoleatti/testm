package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
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
		FormDataListUiHandlers, FilterReadyEvent.MyHandler {


	private static final int PAGE_SIZE = 20;
	private static boolean IS_FIRST_TIME = false;
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
			FilterPresenter filterPresenter) {
		super(eventBus, view, proxy, placeManager, dispatcher, filterPresenter);
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
			super.prepareFromRequest(request);
			filterPresenter.initFilter(TaxType.valueOf(request.getParameter(
					"nType", "")));
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
		final int LOAD_DATA_FROM_FIRST_RECORD = 0;
		filterFormData.setStartIndex(LOAD_DATA_FROM_FIRST_RECORD);
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
	public void onApplyFilter() {
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
	public void onCreateClicked() {
		FormDataFilter filterFormData = filterPresenter.getFilterData();
		if(filterFormData.getDepartmentId().size() > 1){
			MessageEvent.fire(FormDataListPresenter.this, "Для создания налоговой формы необходимо" +
					" указать только одно подразделение");
		} else {
		placeManager.revealPlace(new PlaceRequest(FormDataPresenter.NAME_TOKEN)
				.with(FormDataPresenter.READ_ONLY, "false")
				.with(FormDataPresenter.FORM_DATA_ID,
						String.valueOf(Long.MAX_VALUE))
				.with(FormDataPresenter.FORM_DATA_KIND_ID,
						String.valueOf(filterFormData.getFormDataKind()!=null ? filterFormData.getFormDataKind().getId() : null))
				.with(FormDataPresenter.DEPARTMENT_ID,
						String.valueOf(filterFormData.getDepartmentId()!=null ? filterFormData.getDepartmentId()
								.iterator().next() : null))
				.with(FormDataPresenter.FORM_DATA_TYPE_ID,
						String.valueOf(filterFormData.getFormTypeId()!=null ? filterFormData.getFormTypeId() : null)));
		}
	}

	@Override
	public void onSortingChanged(){
		refreshTable();
	}

	private class TableDataProvider extends AsyncDataProvider<FormDataSearchResultItem> {

		private int ZERO_RECORDS_COUNT = 0;

		public void update() {
			for (HasData<FormDataSearchResultItem> display: getDataDisplays()) {
				onRangeChanged(display);
			}
		}

		@Override
		protected void onRangeChanged(HasData<FormDataSearchResultItem> display) {
			final Range range = display.getVisibleRange();
			GetFormDataList requestData = createRequestData(range);
			dispatcher.execute(requestData,
					new AbstractCallback<GetFormDataListResult>() {
						@Override
						public void onReqSuccess(GetFormDataListResult result) {
							if(result == null || result.getTotalCountOfRecords() == ZERO_RECORDS_COUNT){
								getView().setFormDataList(range.getStart(), ZERO_RECORDS_COUNT,
										new ArrayList<FormDataSearchResultItem>());
							} else {
								handleResponse(result, range);
							}
						}
					});
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
			if(IS_FIRST_TIME){
				FormDataFilter filter = filterPresenter.getFilterData();
				filter.setCountOfRecords(PAGE_SIZE);
				filter.setStartIndex(range.getStart());
				filter.setAscSorting(getView().isAscSorting());
				filter.setSearchOrdering(getView().getSearchOrdering());
				GetFormDataList request = new GetFormDataList();
				request.setFormDataFilter(filter);
				return request;
			}
			IS_FIRST_TIME = true;
			return (new GetFormDataList());
		}

		private void handleResponse(GetFormDataListResult response, Range range) {
			getView().setFormDataList(range.getStart(), response.getTotalCountOfRecords(), response.getRecords());
		}
	}

}
