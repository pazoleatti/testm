package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.*;

public class FilterPresenter extends PresenterWidget<FilterPresenter.MyView> {

	public interface MyView extends View {
		void setDataFilter(FormDataFilter formDataFilter);

		FormDataFilter getDataFilter();

		void setKindList(List<FormDataKind> list);

		void setFormStateList(List<WorkflowState> list);

		void setFormTypesMap(Map<Integer, String> formTypesMap);

		void setDepartmentMaps(Map<Integer, String> departmentMaps);

		void setReportPeriodMaps(Map<Integer, String> reportPeriodMaps);

	}

	private final DispatchAsync dispatchAsync;
	private static final int DEFAULT_DEPARTMENT_ITEM = 0;
	
	private TaxType taxType;

	@Inject
	public FilterPresenter(EventBus eventBus, MyView view,
			DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
	}

	public FormDataFilter getFilterData() {
		FormDataFilter formDataFilter = getView().getDataFilter();
		formDataFilter.setTaxType(this.taxType);
		return formDataFilter;
	}

	public void initFilter(TaxType taxType) {
		this.taxType = taxType;
		GetFilterData action = new GetFilterData();
		action.setTaxType(taxType);
		dispatchAsync.execute(action,
				new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onReqSuccess(GetFilterDataResult result) {

						getView().setFormTypesMap(fillFormTypesMap(result));
						getView().setReportPeriodMaps(fillReportPeriodsMap(result));
						getView().setDepartmentMaps(fillDepartmentsMap(result));
						getView().setKindList(fillFormKindList());
						getView().setFormStateList(fillFormStateList());

						FormDataFilter formDataFilter = new FormDataFilter();
						formDataFilter.setDepartmentId(result.getDepartments().get(DEFAULT_DEPARTMENT_ITEM).getId());
						getView().setDataFilter(formDataFilter);
						FilterReadyEvent.fire(FilterPresenter.this);
					}
				});

	}

	private Map<Integer, String> fillDepartmentsMap(GetFilterDataResult source){
		Map<Integer, String> departmentsMap = new HashMap<Integer, String>();
		for(Department department : source.getDepartments()){
			departmentsMap.put(department.getId(), department.getName());
		}
		return departmentsMap;
	}

	private Map<Integer, String> fillReportPeriodsMap(GetFilterDataResult source){
		Map<Integer, String> reportPeriodsMap = new HashMap<Integer, String>();
		for(ReportPeriod reportPeriod : source.getPeriods()){
			reportPeriodsMap.put(reportPeriod.getId(), reportPeriod.getName());
		}
		return reportPeriodsMap;
	}

	private Map<Integer, String> fillFormTypesMap(GetFilterDataResult source){
		Map<Integer, String> formTypesMap = new HashMap<Integer, String>();
		for(FormType formType : source.getFormTypes()){
			formTypesMap.put(formType.getId(), formType.getName());
		}
		return formTypesMap;
	}

	private List<FormDataKind> fillFormKindList(){
		List<FormDataKind> kind = new ArrayList<FormDataKind>();
		kind.add(null);
		kind.addAll(Arrays.asList(FormDataKind.values()));
		return kind;
	}

	private List<WorkflowState> fillFormStateList(){
		List<WorkflowState> formState = new ArrayList<WorkflowState>();
		formState.add(null);
		formState.addAll(Arrays.asList(WorkflowState.values()));
		return formState;
	}

}
