package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.events.FormDataListApplyEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.events.FormDataListCreateEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FilterPresenter extends PresenterWidget<FilterPresenter.MyView> implements FilterUIHandlers {

	public interface MyView extends View, HasUiHandlers<FilterUIHandlers> {
		void setDataFilter(FormDataFilter formDataFilter);

		FormDataFilter getDataFilter();

		void updateReportPeriodPicker();

		void updateDepartmentPicker();

		void setKindList(List<FormDataKind> list);

		void setFormStateList(List<WorkflowState> list);

		void setFormTypesMap(Map<Integer, String> formTypesMap);

		void setDepartmentsList(List<Department> list, Set<Integer> availableValues);

		void setSelectedDepartments(List<Integer> values);

		void setSelectedReportPeriods(List<Integer> reportPeriodList);

		List<Integer> getSelectedDepartments();

		void setReportPeriods(List<ReportPeriod> reportPeriods);

		List<Integer> getSelectedReportPeriods();
	}

	private final DispatchAsync dispatchAsync;
	private static Set<ReportPeriod> periods = new HashSet<ReportPeriod>();
	private static Map<TaxType, FormDataFilter> savedFilterData = new HashMap<TaxType, FormDataFilter>();
	private static Map<TaxType, List<Integer>> savedDepartmentsMap = new HashMap<TaxType, List<Integer>>();

	private TaxType taxType;

	@Inject
	public FilterPresenter(EventBus eventBus, MyView view,
	                       DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
		initSavedFilterDataMap();
	}

	public FormDataFilter getFilterData() {
		FormDataFilter formDataFilter = getView().getDataFilter();
		formDataFilter.setReportPeriodIds(getView().getSelectedReportPeriods());
		if (getView().getSelectedDepartments() == null) {
			formDataFilter.setDepartmentId(new ArrayList<Integer>());
		} else {
			formDataFilter.setDepartmentId(getView().getSelectedDepartments());
		}
		formDataFilter.setTaxType(this.taxType);
		return formDataFilter;
	}

	public List<ReportPeriod> getReportPeriods() {
		return new ArrayList<ReportPeriod>(periods);
	}

	public void updateSavedFilterData(FormDataFilter formDataFilter){
		savedFilterData.put(this.taxType, formDataFilter);
		List<Integer> selectedDepartments = new ArrayList<Integer>(getView().getSelectedDepartments());
		savedDepartmentsMap.put(this.taxType, selectedDepartments);
	}

	public void initFilter(final TaxType taxType) {
		this.taxType = taxType;
		getView().updateReportPeriodPicker();
		getView().updateDepartmentPicker();

		GetFilterData action = new GetFilterData();
		action.setTaxType(taxType);
		dispatchAsync.execute(action, CallbackUtils
				.wrongStateCallback(new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onSuccess(GetFilterDataResult result) {
						FormDataFilterAvailableValues filterValues = result.getFilterValues();
						getView().setDepartmentsList(result.getDepartments(), filterValues.getDepartmentIds());
						getView().setKindList(fillFormKindList(filterValues.getKinds()));
						getView().setFormTypesMap(fillFormTypesMap(filterValues.getFormTypes()));
						getView().setReportPeriods(result.getReportPeriods());
						getView().setFormStateList(fillFormStateList());
						getView().setDataFilter(prepareFormDataFilter(result));
						FilterReadyEvent.fire(FilterPresenter.this);
					}
				}, this));
	}

	@Override
	public TaxType getCurrentTaxType(){
		return this.taxType;
	}

	@Override
	public void onCreateClicked() {
		FormDataListCreateEvent.fire(this);
	}

	@Override
	public void onApplyClicked() {
		FormDataListApplyEvent.fire(this);
	}

	private FormDataFilter prepareFormDataFilter(GetFilterDataResult result){
		FormDataFilter formDataFilter = new FormDataFilter();
		if(savedFilterData.get(taxType) == null){
			if(result.getFilterValues().getDepartmentIds() != null && !result.getFilterValues().getDepartmentIds().isEmpty()){
				Integer departmentId = result.getFilterValues().getDefaultDepartmentId();
				//String departmentName = getDepartmentNameById(result.getDepartments(), departmentId);
				//Если пользователь ни разу не выполнял фильтрацию, то ставим значения фильтра по-умолчанию
				List<Integer> defaultDepartment = new ArrayList<Integer>(Arrays.asList(departmentId));
				List<Integer> defaultSelectedDepartment = new ArrayList<Integer>();
				defaultSelectedDepartment.add(departmentId);
				getView().setSelectedDepartments(defaultSelectedDepartment);
				formDataFilter.setDepartmentId(defaultDepartment);
			} else {
				formDataFilter.setDepartmentId(null);
			}
		} else {
			//В противном случае - заполняем фильтр значениями, по которым делалась фильтрация в последний раз,
			formDataFilter = savedFilterData.get(taxType);
			getView().setSelectedDepartments(savedDepartmentsMap.get(taxType));
		}
		return formDataFilter;
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

	private void initSavedFilterDataMap(){
		for(TaxType value : TaxType.values()){
			savedFilterData.put(value, null);
			savedDepartmentsMap.put(value, new ArrayList<Integer>());
		}
	}


}
