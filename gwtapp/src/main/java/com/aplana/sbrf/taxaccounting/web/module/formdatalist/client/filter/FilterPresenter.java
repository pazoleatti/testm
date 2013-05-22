package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.*;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.events.*;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.*;
import com.google.inject.*;
import com.google.web.bindery.event.shared.*;
import com.gwtplatform.dispatch.shared.*;
import com.gwtplatform.mvp.client.*;

import java.util.*;

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

		void setSelectedDepartments(Map<String, Integer> values);

		void setSelectedReportPeriods(List<ReportPeriod> reportPeriodList);

		Map<String, Integer> getSelectedDepartments();

		void setTaxPeriods(List<TaxPeriod> taxPeriods);

		void setReportPeriods(List<ReportPeriod> reportPeriods);

		List<Integer> getSelectedReportPeriods();
	}

	private final DispatchAsync dispatchAsync;
	private static Set<ReportPeriod> periods = new HashSet<ReportPeriod>();
	private static Map<TaxType, FormDataFilter> savedFilterData = new HashMap<TaxType, FormDataFilter>();
	private static Map<TaxType, Map<String, Integer>> savedDepartmentsMap = new HashMap<TaxType, Map<String, Integer>>();

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
		formDataFilter.setReportPeriodIds(new ArrayList<Integer>(getView().getSelectedReportPeriods()));
		formDataFilter.setDepartmentId(new ArrayList<Integer>(getView().getSelectedDepartments().values()));
		formDataFilter.setTaxType(this.taxType);
		return formDataFilter;
	}

	public List<ReportPeriod> getReportPeriods() {
		return new ArrayList<ReportPeriod>(periods);
	}

	public void updateSavedFilterData(FormDataFilter formDataFilter){
		savedFilterData.put(this.taxType, formDataFilter);
		Map<String, Integer> selectedDepartments = new HashMap<String, Integer>();
		selectedDepartments.putAll(getView().getSelectedDepartments());
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
						if(filterValues.getDepartmentIds() == null){
							//Контролер УНП
							getView().setDepartmentsList(result.getDepartments(), convertDepartmentsToIds(result.getDepartments()));
						} else {
							getView().setDepartmentsList(result.getDepartments(), filterValues.getDepartmentIds());
						}
						getView().setKindList(fillFormKindList(filterValues.getKinds()));
						getView().setFormTypesMap(fillFormTypesMap(filterValues.getFormTypes()));
						getView().setTaxPeriods(result.getTaxPeriods());
						getView().setFormStateList(fillFormStateList());
						getView().setDataFilter(prepareFormDataFilter(result));
						FilterReadyEvent.fire(FilterPresenter.this);
					}
				}, this));
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		GetReportPeriods action = new GetReportPeriods();
		action.setTaxPeriod(taxPeriod);
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetReportPeriodsResult>() {
					@Override
					public void onSuccess(GetReportPeriodsResult result) {
						periods.addAll(result.getReportPeriods());
						getView().setReportPeriods(result.getReportPeriods());
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
				String departmentName = getDepartmentNameById(result.getDepartments(), departmentId);
				//Если пользователь ни разу не выполнял фильтрацию, то ставим значения фильтра по-умолчанию
				List<Integer> defaultDepartment = new ArrayList<Integer>(Arrays.asList(departmentId));
				Map<String, Integer> defaultSelectedDepartment = new HashMap<String, Integer>();
				defaultSelectedDepartment.put(departmentName, departmentId);
				getView().setSelectedDepartments(defaultSelectedDepartment);
				formDataFilter.setDepartmentId(defaultDepartment);
			} else {
				formDataFilter.setDepartmentId(null);
			}
			if (result.getCurrentReportPeriod() != null) {
				getView().setSelectedReportPeriods(Arrays.asList(result.getCurrentReportPeriod()));
				formDataFilter.setReportPeriodIds(Arrays.asList(result.getCurrentReportPeriod().getId()));
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

	private Set<Integer> convertDepartmentsToIds(List<Department> source){
		Set<Integer> result = new HashSet<Integer>();
		for(Department department : source){
			result.add(department.getId());
		}
		return result;
	}

	private String getDepartmentNameById(List<Department> departmentList, Integer id){
		for(Department department : departmentList){
			if (department.getId() == id){
				return department.getName();
			}
		}
		return null;
	}

	private void initSavedFilterDataMap(){
		for(TaxType value : TaxType.values()){
			savedFilterData.put(value, null);
			savedDepartmentsMap.put(value, new HashMap<String, Integer>());
		}
	}
}
