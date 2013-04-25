package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.*;

public class DeclarationFilterPresenter extends PresenterWidget<DeclarationFilterPresenter.MyView>
		implements DeclarationFilterUIHandlers {

	public interface MyView extends View, HasUiHandlers<DeclarationFilterUIHandlers> {
		void setDataFilter(DeclarationDataFilter formDataFilter, TaxType taxType);

		void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments);

		List<Integer> getSelectedReportPeriods();

		void setSelectedReportPeriods(List<ReportPeriod> reportPeriodList);

		void updateReportPeriodPicker();

		void updateDepartmentPicker();

		Map<String, Integer> getSelectedDepartments();

		Integer getSelectedDeclarationTypeId();

		void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap);

		void setTaxPeriods(List<TaxPeriod> taxPeriods);

		void setReportPeriods(List<ReportPeriod> reportPeriods);

		void setSelectedDepartments(Map<String, Integer> values);
	}

	private final DispatchAsync dispatchAsync;
	
	private TaxType taxType;
	private static Map<TaxType, DeclarationDataFilter> savedFilterData = new HashMap<TaxType, DeclarationDataFilter>();
	private static Map<TaxType, Map<String, Integer>> savedDepartmentsMap = new HashMap<TaxType, Map<String, Integer>>();
	private List<TARole> userRoles = null;
	private List<TaxPeriod> taxPeriods;
	private List<Department> departments;
	private Set<ReportPeriod> periods = new HashSet<ReportPeriod>();
	private ReportPeriod currentReportPeriod;
	private DeclarationDataFilterAvailableValues filterValues;

	@Inject
	public DeclarationFilterPresenter(EventBus eventBus, MyView view,
	                                  DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
		detectUserRoles();
	}

	public DeclarationDataFilter getFilterData() {
		DeclarationDataFilter declarationFilter = new DeclarationDataFilter();

		declarationFilter.setReportPeriodIds(new ArrayList<Integer>(getView().getSelectedReportPeriods()));
		declarationFilter.setDepartmentIds(new ArrayList<Integer>(getView().getSelectedDepartments().values()));
		declarationFilter.setTaxType(this.taxType);
		declarationFilter.setDeclarationTypeId(getView().getSelectedDeclarationTypeId());
		return declarationFilter;
	}

	public DeclarationDataFilterAvailableValues getFilterValues() {
		return filterValues;
	}

	public List<TaxPeriod> getTaxPeriods() {
		return taxPeriods;
	}

	public List<Department> getDepartments() {
		return departments;
	}

	public ReportPeriod getCurrentReportPeriod() {
		if (getView().getSelectedReportPeriods().size() == 1) {
			int firstSelectedPeriodId = getView().getSelectedReportPeriods().get(0);
			for (ReportPeriod period : periods) {
				if (period.getId() == firstSelectedPeriodId) {
					return period;
				}
			}
			return currentReportPeriod;
		}
		return null;
	}

	public void updateSavedFilterData(DeclarationDataFilter declarationFilter){
		savedFilterData.put(this.taxType, declarationFilter);
		Map<String, Integer> selectedDepartments = new HashMap<String, Integer>();
		selectedDepartments.putAll(getView().getSelectedDepartments());
		savedDepartmentsMap.put(this.taxType, selectedDepartments);
	}

	public void initFilter(final TaxType taxType) {
        this.taxType = taxType;
		getView().updateReportPeriodPicker();
		getView().updateDepartmentPicker();

		GetDeclarationFilterData action = new GetDeclarationFilterData();
        action.setTaxType(taxType);
        dispatchAsync.execute(action, CallbackUtils
				        .defaultCallback(new AbstractCallback<GetDeclarationFilterDataResult>() {
						@Override
						public void onSuccess(GetDeclarationFilterDataResult result) {
							filterValues = result.getFilterValues();
							taxPeriods = result.getTaxPeriods();
							departments = result.getDepartments();
							currentReportPeriod = result.getCurrentReportPeriod();

							getView().setDepartmentsList(departments, filterValues.getDepartmentIds());
							getView().setTaxPeriods(taxPeriods);
							getView().setDeclarationTypeMap(fillDeclarationTypesMap(filterValues));

							getView().setDataFilter(prepareFormDataFilter(), taxType);
							DeclarationFilterReadyEvent.fire(DeclarationFilterPresenter.this);
						}
					}));
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
				}));
	}

	@Override
	public TaxType getCurrentTaxType(){
		return this.taxType;
	}

	private Map<Integer, String> fillDeclarationTypesMap(DeclarationDataFilterAvailableValues source){
		Map<Integer, String> declarationTypeMap = new LinkedHashMap<Integer, String>();
		declarationTypeMap.put(null, "");
		for(DeclarationType declarationType : source.getDeclarationTypes()){
			declarationTypeMap.put(declarationType.getId(), declarationType.getName());
		}
		return declarationTypeMap;
	}

	private DeclarationDataFilter prepareFormDataFilter(){
		DeclarationDataFilter formDataFilter = new DeclarationDataFilter();

		if(savedFilterData.get(taxType) == null){
			if(filterValues.getDepartmentIds() != null && !filterValues.getDepartmentIds().isEmpty()
					&& !isControlOfUnp() ){
				Integer departmentId = filterValues.getDefaultDepartmentId();
				String departmentName = getDepartmentNameById(departments, departmentId);
				//Если пользователь ни разу не выполнял фильтрацию, то ставим значения фильтра по-умолчанию
				List<Integer> defaultDepartment = new ArrayList<Integer>(Arrays.asList(departmentId));
				Map<String, Integer> defaultSelectedDepartment = new HashMap<String, Integer>();
				defaultSelectedDepartment.put(departmentName, departmentId);
				getView().setSelectedDepartments(defaultSelectedDepartment);
				formDataFilter.setDepartmentIds(defaultDepartment);
			} else {
				formDataFilter.setDepartmentIds(null);
			}
			if (currentReportPeriod != null) {
				getView().setSelectedReportPeriods(Arrays.asList(currentReportPeriod));
				formDataFilter.setReportPeriodIds(Arrays.asList(currentReportPeriod.getId()));
			}
		} else {
			//В противном случае - заполняем фильтр значениями, по которым делалась фильтрация в последний раз,
			formDataFilter = savedFilterData.get(taxType);
			getView().setSelectedDepartments(savedDepartmentsMap.get(taxType));
		}
		return formDataFilter;
	}

	private String getDepartmentNameById(List<Department> departmentList, Integer id){
		for(Department department : departmentList){
			if (department.getId() == id){
				return department.getName();
			}
		}
		return null;
	}

	private boolean isControlOfUnp(){
		if(userRoles != null){
			for(TARole taRole : userRoles){
				if(taRole.getAlias().equals(TARole.ROLE_CONTROL_UNP)){
					return true;
				}
			}
		}
		return false;
	}

	private void detectUserRoles(){
		DetectUserRoleAction action = new DetectUserRoleAction();
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<DetectUserRoleResult>() {
					@Override
					public void onSuccess(DetectUserRoleResult result) {
						userRoles = result.getUserRole();
					}
				}));
	}

}
