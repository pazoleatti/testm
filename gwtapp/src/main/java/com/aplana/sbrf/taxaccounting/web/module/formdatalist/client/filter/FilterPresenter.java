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

		void setReportPeriodMaps(Map<Integer, String> reportPeriodMaps);

		void setDepartmentsList(List<Department> list);

		void setSelectedDepartments(Map<String, Integer> values);

		Map<String, Integer> getSelectedDepartments();

	}

	private final DispatchAsync dispatchAsync;
	private static final int DEFAULT_DEPARTMENT_ITEM = 0;
    private static Map<TaxType, FormDataFilter> savedFilterData = new HashMap<TaxType, FormDataFilter>();
	private static Map<TaxType, Map<String, Integer>> savedDepartmentsMap = new HashMap<TaxType, Map<String, Integer>>();
	
	private TaxType taxType;

	@Inject
	public FilterPresenter(EventBus eventBus, MyView view,
			DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
        initSavedFilterDataMap();
	}

	public FormDataFilter getFilterData() {
		FormDataFilter formDataFilter = getView().getDataFilter();
		formDataFilter.setDepartmentId(new ArrayList<Integer>(getView().getSelectedDepartments().values()));
		formDataFilter.setTaxType(this.taxType);
		return formDataFilter;
	}

    public void updateSavedFilterData(FormDataFilter formDataFilter){
        savedFilterData.put(this.taxType, formDataFilter);
	    Map<String, Integer> selectedDepartments = new HashMap<String, Integer>();
	    selectedDepartments.putAll(getView().getSelectedDepartments());
	    savedDepartmentsMap.put(this.taxType, selectedDepartments);
    }

	public void initFilter(final TaxType taxType) {
        this.taxType = taxType;
        GetFilterData action = new GetFilterData();
        action.setTaxType(taxType);
        dispatchAsync.execute(action,
                new AbstractCallback<GetFilterDataResult>() {
                    @Override
                    public void onReqSuccess(GetFilterDataResult result) {

                        getView().setFormTypesMap(fillFormTypesMap(result));
                        getView().setReportPeriodMaps(fillReportPeriodsMap(result));
                        getView().setKindList(fillFormKindList());
                        getView().setFormStateList(fillFormStateList());
	                    getView().setDepartmentsList(result.getDepartments());

                        FormDataFilter formDataFilter = new FormDataFilter();
                        if(savedFilterData.get(taxType) == null){
	                        Integer departmentId = result.getDepartments().get(DEFAULT_DEPARTMENT_ITEM).getId();
	                        String departmentName = result.getDepartments().get(DEFAULT_DEPARTMENT_ITEM).getName();
                            //Если пользователь ни разу не выполнял фильтрацию, то ставим значения фильтра по-умолчанию
	                        List<Integer> defaultDepartment = new ArrayList<Integer>();
	                        defaultDepartment.add(departmentId);

	                        Map<String, Integer> defaultSelectedDepartment = new HashMap<String, Integer>();
	                        defaultSelectedDepartment.put(departmentName, departmentId);
	                        getView().setSelectedDepartments(defaultSelectedDepartment);
                            formDataFilter.setDepartmentId(defaultDepartment);
                            if (result.getCurrentReportPeriodId() != null) {
                                formDataFilter.setReportPeriodId(result.getCurrentReportPeriodId());
                            }

                        } else {
	                        //В противном случае - заполняем фильтр значениями, по которым делалась фильтрация в последний раз,
                            formDataFilter = savedFilterData.get(taxType);
	                        getView().setSelectedDepartments(savedDepartmentsMap.get(taxType));
                        }

                        getView().setDataFilter(formDataFilter);
                        FilterReadyEvent.fire(FilterPresenter.this);
                    }
                });
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

    private void initSavedFilterDataMap(){
        for(TaxType taxType : TaxType.values()){
            savedFilterData.put(taxType, null);
	        savedDepartmentsMap.put(taxType, new HashMap<String, Integer>());
        }
    }
}
