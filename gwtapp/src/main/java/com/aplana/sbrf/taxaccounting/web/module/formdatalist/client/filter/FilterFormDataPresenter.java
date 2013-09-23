package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
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

		void setFormTypesMap(Map<Integer, String> formTypesMap);

		void setDepartments(List<Department> list, Set<Integer> availableValues);

		void setReportPeriods(List<ReportPeriod> reportPeriods);

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
						getView().setKindList(fillFormKindList(filterValues.getKinds()));
						getView().setFormTypesMap(fillFormTypesMap(filterValues.getFormTypes()));
						getView().setReportPeriods(result.getReportPeriods());
						getView().setFormStateList(fillFormStateList());
						getView().setReturnStateList(Arrays.asList(new Boolean[]{null, Boolean.TRUE, Boolean.FALSE}));
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

	@Override
	public void onCreateClicked() {
		FormDataListCreateEvent.fire(this);
	}

	@Override
	public void onApplyClicked() {
		FormDataListApplyEvent.fire(this);
	}

	private Map<Integer, String> fillFormTypesMap(List<FormType> source){
		Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();
		formTypesMap.put(null, "");
		for(FormType formType : source){
			formTypesMap.put(formType.getId(), formType.getName());
		}
		return formTypesMap;
	}

	private List<FormDataKind> fillFormKindList(List<FormDataKind> source){
		List<FormDataKind> kind = new ArrayList<FormDataKind>();
		kind.add(null);
		kind.addAll(source);
		return kind;
	}

	private List<WorkflowState> fillFormStateList(){
		List<WorkflowState> formState = new ArrayList<WorkflowState>();
		formState.add(null);
		formState.addAll(Arrays.asList(WorkflowState.values()));
		return formState;
	}

}
