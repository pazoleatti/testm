package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.ErrorEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.TitleUpdateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
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

import java.util.HashMap;
import java.util.Map;

public class FormDataListPresenter extends
		FormDataListPresenterBase<FormDataListPresenter.MyProxy> implements
		FormDataListUiHandlers, FilterReadyEvent.MyHandler {

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
			ErrorEvent.fire(this, "Неудалось открыть список форм", e);
		}
	}

	/**
	 * Применение фильтра, обновление списка форм
	 * 
	 * @param filterFormData
	 */
	private void loadFormDataList(final FormDataFilter filterFormData) {
		
		GetFormDataList action = new GetFormDataList();
		action.setFormDataFilter(filterFormData);
		dispatcher.execute(action,
				new AbstractCallback<GetFormDataListResult>() {
					@Override
					public void onReqSuccess(GetFormDataListResult result) {
						Map<Integer, String> departmentMap = new HashMap<Integer, String>();
						for (Department department : result.getDepartments()) {
							departmentMap.put(department.getId(),
									department.getName());
						}

						Map<Integer, String> reportPeriodMap = new HashMap<Integer, String>();
						for (ReportPeriod period : result.getReportPeriods()) {
							reportPeriodMap.put(period.getId(),
									period.getName());
						}

						getView().setFormDataList(result.getRecords());
						getView().setDepartmentMap(departmentMap);
						getView().setReportPeriodMap(reportPeriodMap);

						TitleUpdateEvent.fire(this, "Список налоговых форм", filterFormData.getTaxType().getName());
						// Вручную вызывается onReveal. Вызываем его всегда,
						// даже когда
						// презентер в состоянии visible, т.к. нам необходима
						// его разблокировка.
						// Почему GWTP вызывает блокировку даже если страница
						// уже видна - непонятно.
						getProxy().manualReveal(FormDataListPresenter.this);

					}
				});
	}

	@Override
	public void onApplyFilter() {
		FormDataFilter filterFormData = filterPresenter.getFilterData();
		loadFormDataList(filterFormData);
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

	@Override
	public void onCreateClicked() {
		FormDataFilter filterFormData = filterPresenter.getFilterData();
		placeManager.revealPlace(new PlaceRequest(FormDataPresenter.NAME_TOKEN)
				.with(FormDataPresenter.READ_ONLY, "false")
				.with(FormDataPresenter.FORM_DATA_ID,
						String.valueOf(Long.MAX_VALUE))
				.with(FormDataPresenter.FORM_DATA_KIND_ID,
						String.valueOf(filterFormData.getFormDataKind().getId()))
				.with(FormDataPresenter.DEPARTMENT_ID,
						String.valueOf(filterFormData.getDepartmentId()))
				.with(FormDataPresenter.FORM_DATA_TYPE_ID,
						String.valueOf(filterFormData.getFormTypeId()))
				.with(FormDataPresenter.FORM_DATA_RPERIOD_ID,
						String.valueOf(filterFormData.getReportPeriodId())));
	}

}
