package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FilterFormDataPresenter extends PresenterWidget<FilterFormDataPresenter.MyView> implements FilterFormDataUIHandlers {

	public interface MyView extends View, HasUiHandlers<FilterFormDataUIHandlers> {
		
		// Установка/получение значений фильтра
		
		void setDataFilter(FormDataFilter formDataFilter);

		FormDataFilter getDataFilter();
		
		// Установка доступных значений

		void setKindList(List<FormDataKind> list);

		void setFormStateList(List<WorkflowState> list);

		void setReturnStateList(List<Boolean> list);

		void setFormTypesMap(List<FormType> formTypes);

		void setDepartments(List<Department> list, Set<Integer> availableValues);

		void setReportPeriods(List<ReportPeriod> reportPeriods);

		void setElementNames(Map<FormDataElementName, String> names);

	}

	private final DispatchAsync dispatchAsync;

	@Inject
	public FilterFormDataPresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	public FormDataFilter getFilterData() {
		return getView().getDataFilter();
	}

	public void updateSavedFilterData(FormDataFilter formDataFilter){
		getView().setDataFilter(formDataFilter);
	}

	public void initFilter(final TaxType taxType, final FormDataFilter filter) {
		GetFilterData action = new GetFilterData();
		action.setTaxType(taxType);
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onSuccess(GetFilterDataResult result) {
						FormDataFilterAvailableValues filterValues = result.getFilterValues();
						getView().setDepartments(result.getDepartments(), filterValues.getDepartmentIds());
						getView().setKindList(filterValues.getKinds());
						getView().setFormTypesMap(filterValues.getFormTypes());
						getView().setReportPeriods(result.getReportPeriods());
						getView().setFormStateList(Arrays.asList(WorkflowState.values()));
						getView().setReturnStateList(Arrays.asList(new Boolean[]{ Boolean.TRUE, Boolean.FALSE }));
						// Если при инициализации фильтра клиент устанавливает фильтр, то берем его.
						// Иначе устанавливаем фильтр по умолчанию.
						if (filter != null){
							getView().setDataFilter(filter);
						} else {
							getView().setDataFilter(result.getDefaultFilter());
						}
						FilterFormDataReadyEvent.fire(FilterFormDataPresenter.this, true);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						super.onFailure(caught);
						FilterFormDataReadyEvent.fire(FilterFormDataPresenter.this, false);
					}
					
				}, this));
	}

	public void changeFilterElementNames(TaxType taxType) {
		GetFieldsNames action = new GetFieldsNames();
		action.setTaxType(taxType);
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFieldsNamesResult>() {
					@Override
					public void onSuccess(GetFieldsNamesResult result) {
						getView().setElementNames(result.getFieldNames());
					}
				}, this)
		);
	}

	@Override
	public void onCreateClicked() {
		FormDataListCreateEvent.fire(this);
	}

	@Override
	public void onApplyClicked() {
		FormDataListApplyEvent.fire(this);
	}

}
