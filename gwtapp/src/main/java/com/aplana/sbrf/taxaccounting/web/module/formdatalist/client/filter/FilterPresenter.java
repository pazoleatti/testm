package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter.SelectItem;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterPresenter extends PresenterWidget<FilterPresenter.MyView> {

	public interface MyView extends View {
		void setDataFilter(FormDataFilter formDataFilter);

		FormDataFilter getDataFilter();

		void setPeriodList(List<SelectItem<Integer>> list);

		void setDepartmentList(List<SelectItem<Integer>> list);

		void setFormtypeList(List<SelectItem<Integer>> list);

		public void setKindList(List<SelectItem<FormDataKind>> list);

		public void setFormStateList(List<SelectItem<WorkflowState>> list);

		void setFormTypesMap(Map<Integer, String> formTypesMap);

		void setDepartmentMaps(Map<Integer, String> departmentMaps);

		void setReportPeriodMaps(Map<Integer, String> reportPeriodMaps);

	}

	private final DispatchAsync dispatchAsync;
	private static final int DEFAULT_DEPARTMENT_SELECTED_ITEM = 1;

	@Inject
	public FilterPresenter(EventBus eventBus, MyView view,
			DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
	}

	public FormDataFilter getFilterData() {
		return getView().getDataFilter();
	}

	public void initFilter(TaxType taxType) {
		GetFilterData action = new GetFilterData();
		action.setTaxType(taxType);
		dispatchAsync.execute(action,
				new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onReqSuccess(GetFilterDataResult result) {

						List<SelectItem<Integer>> reportPeriodItems = fillPeriodList(result);
						List<SelectItem<Integer>> departmentItems = fillDepartmentList(result);
						List<SelectItem<Integer>> formTypeItems = fillFormTypeList(result);
						List<SelectItem<FormDataKind>> formKindList = fillFormKindList();
						List<SelectItem<WorkflowState>> formStateList = fillFormStateList();

						Map<Integer, String> formTypesMap = new HashMap<Integer, String>();
						Map<Integer, String> reportDepiodMaps = new HashMap<Integer, String>();
						Map<Integer, String> departmentMaps = new HashMap<Integer, String>();

						for (FormType formType : result.getFormTypes()){
							formTypesMap.put(formType.getId(), formType.getName());
						}
						for(Department department : result.getDepartments()){
							departmentMaps.put(department.getId(), department.getName());
						}
						for(ReportPeriod reportPeriod : result.getPeriods()){
							reportDepiodMaps.put(reportPeriod.getId(), reportPeriod.getName());
						}
						getView().setFormTypesMap(formTypesMap);
						getView().setDepartmentMaps(departmentMaps);
						getView().setReportPeriodMaps(reportDepiodMaps);

						getView().setFormtypeList(formTypeItems);
						getView().setPeriodList(reportPeriodItems);
						getView().setDepartmentList(departmentItems);
						getView().setKindList(formKindList);
						getView().setFormStateList(formStateList);

						FormDataFilter formDataFilter = new FormDataFilter();
						formDataFilter.setDepartmentId(departmentItems.get(DEFAULT_DEPARTMENT_SELECTED_ITEM).getId());
						getView().setDataFilter(formDataFilter);
						FilterReadyEvent.fire(FilterPresenter.this);
					}
				});

	}

	private List<SelectItem<Integer>> fillPeriodList(GetFilterDataResult result){
		List<ReportPeriod> reportPeriods = result.getPeriods();
		List<SelectItem<Integer>> reportPeriodItems = new ArrayList<SelectItem<Integer>>();
		reportPeriodItems.add(new SelectItem<Integer>(null, ""));
		for (ReportPeriod reportPeriod : reportPeriods){
			reportPeriodItems.add(new SelectItem<Integer>( reportPeriod.getId(),
					reportPeriod.getName()));
		}
		return reportPeriodItems;
	}

	private List<SelectItem<Integer>> fillFormTypeList(GetFilterDataResult result){
		List<FormType> formTypes = result.getFormTypes();
		List<SelectItem<Integer>> formTypeItems = new ArrayList<SelectItem<Integer>>();
		formTypeItems.add(new SelectItem<Integer>(null, ""));
		for (FormType formType : formTypes) {
			formTypeItems.add(new SelectItem<Integer>( formType.getId(), formType
					.getName()));
		}
		return formTypeItems;
	}

	private List<SelectItem<Integer>> fillDepartmentList(GetFilterDataResult result){
		List<Department> departments = result.getDepartments();
		List<SelectItem<Integer>> departmentItems = new ArrayList<SelectItem<Integer>>();

		departmentItems.add(new SelectItem<Integer>(null, ""));
		for (Department department : departments) {
			departmentItems.add(new SelectItem<Integer>( department.getId(), department
					.getName()));
		}
		return departmentItems;
	}

	private List<SelectItem<FormDataKind>> fillFormKindList(){
		List<SelectItem<FormDataKind>> kind = new ArrayList<SelectItem<FormDataKind>>();
		FormDataKind[] formDataKinds = FormDataKind.values();

		kind.add(new SelectItem<FormDataKind>(null, ""));
		for (int i = 0; i < formDataKinds.length; i++){
			kind.add(new SelectItem<FormDataKind>(formDataKinds[i], formDataKinds[i].getName()));
		}
		return kind;
	}

	private List<SelectItem<WorkflowState>> fillFormStateList(){
		List<SelectItem<WorkflowState>> formState = new ArrayList<SelectItem<WorkflowState>>();
		WorkflowState[] workflowStates = WorkflowState.values();

		formState.add(new SelectItem<WorkflowState>(null, ""));
		for(int i = 0; i < workflowStates.length; i++){
			formState.add(new SelectItem<WorkflowState>(workflowStates[i], workflowStates[i].getName()));
		}

		return formState;
	}

}
