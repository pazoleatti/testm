package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.events.FormDataListApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.events.FormDataListCreateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterReadyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataListResult;
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

public class FormDataListPresenter extends
		FormDataListPresenterBase<FormDataListPresenter.MyProxy> implements
		FormDataListUiHandlers, FilterReadyEvent.MyHandler, FormDataListCreateEvent.FormDataCreateHandler,
		FormDataListApplyEvent.FormDataApplyHandler {

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
		} catch (Exception e) {
			ErrorEvent.fire(this, "Не удалось открыть список форм", e);
		}
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
		getView().updateTitle(filterFormData.getTaxType().getName());
		filterPresenter.updateSavedFilterData(filterFormData);
		getView().updateData(0);
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
			dialogPresenter.initDialog(filterPresenter.getFilterData().getTaxType());
			getView().updateData(0);

			//TitleUpdateEvent.fire(this, "Список налоговых форм", filterFormData.getTaxType().getName());
			// Вручную вызывается onReveal. Вызываем его всегда,
			// даже когда
			// презентер в состоянии visible, т.к. нам необходима
			// его разблокировка.
			// Почему GWTP вызывает блокировку даже если страница
			// уже видна - непонятно.
			getProxy().manualReveal(FormDataListPresenter.this);
		}
	}

	@Override
	public void onSortingChanged(){
		getView().updateData();
	}

	@Override
	public void onRangeChange(final int start, int length) {
		FormDataFilter filter = filterPresenter.getFilterData();
		filter.setCountOfRecords(length);
		filter.setStartIndex(start);
		filter.setAscSorting(getView().isAscSorting());
		filter.setSearchOrdering(getView().getSearchOrdering());
		GetFormDataList request = new GetFormDataList();
		request.setFormDataFilter(filter);
		dispatcher.execute(request, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFormDataListResult>() {
					@Override
					public void onSuccess(GetFormDataListResult result) {
						getView().setTableData(start, result.getTotalCountOfRecords(), result.getRecords());
					}
				}, FormDataListPresenter.this));
	}

}
