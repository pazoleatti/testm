package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriods;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsResult;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DialogPresenter extends PresenterWidget<DialogPresenter.MyView> implements DialogUiHandlers {

	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	private final Map<Integer, FormType> formTypeMap = new HashMap<Integer, FormType>();
	private final Map<Integer, Department> departmentMap = new HashMap<Integer, Department>();
	private final Map<Integer, ReportPeriod> reportPeriodMap = new HashMap<Integer, ReportPeriod>();

	public interface MyView extends PopupView, HasUiHandlers<DialogUiHandlers> {
		void clearInput();
		void setReportPeriods(List<ReportPeriod> reportPeriods);
		void createDepartmentFilter(List<Department> list, Set<Integer> availableValues);
		void createReportPeriodFilter(List<TaxPeriod> taxPeriods);
		void setupUI();
		void setKindList(List<FormDataKind> list);
		void setFormTypeList(List<FormType> list);
		FormDataFilter getFilterData();
		void setFormTypeValue(FormType value);
		void setFormKindValue(FormDataKind value);
		void setDepartmentValue(Map<String, Integer> value);
		void setReportPeriodValue(List<ReportPeriod> value);
	}

	@Inject
	public DialogPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
		super(eventBus, view);
		this.placeManager = placeManager;
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onReveal() {
		super.onReveal();
	}

	@Override
	public void onConfirm() {
		FormDataFilter filterFormData = getView().getFilterData();
		if(isFilterDataCorrect(filterFormData)){
			getView().hide();
			getView().clearInput();

			placeManager.revealPlace(new PlaceRequest(FormDataPresenter.NAME_TOKEN)
					.with(FormDataPresenter.READ_ONLY, "false")
					.with(FormDataPresenter.FORM_DATA_ID, String.valueOf(Long.MAX_VALUE))
					.with(FormDataPresenter.FORM_DATA_KIND_ID, String.valueOf(filterFormData.getFormDataKind() != null
							? filterFormData.getFormDataKind().getId() : null))
					.with(FormDataPresenter.DEPARTMENT_ID, String.valueOf(filterFormData.getDepartmentId() != null
							? filterFormData.getDepartmentId().iterator().next() : null))
					.with(FormDataPresenter.FORM_DATA_TYPE_ID, String.valueOf(filterFormData.getFormTypeId() != null
							? filterFormData.getFormTypeId() : null))
					.with(FormDataPresenter.FORM_DATA_RPERIOD_ID, String.valueOf(filterFormData.getReportPeriodIds()
							.iterator().next()))
			);
		}

	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		GetReportPeriods action = new GetReportPeriods();
		action.setTaxPeriod(taxPeriod);
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetReportPeriodsResult>() {
					@Override
					public void onSuccess(GetReportPeriodsResult result) {
						getView().setReportPeriods(result.getReportPeriods());
					}
				}));
	}

	public void initDialog(TaxType taxType){
		GetFilterData action = new GetFilterData();
		action.setTaxType(taxType);
		dispatchAsync.execute(action, CallbackUtils
				.wrongStateCallback(new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onSuccess(GetFilterDataResult result) {
						FormDataFilterAvailableValues filterValues = result.getFilterValues();

						if (filterValues.getDepartmentIds() == null) {
							//Контролер УНП
							getView().createDepartmentFilter(result.getDepartments(), convertDepartmentsToIds(result.getDepartments()));
						} else {
							getView().createDepartmentFilter(result.getDepartments(), filterValues.getDepartmentIds());
						}
						getView().setKindList(fillFilterList(filterValues.getKinds()));
						getView().setFormTypeList(fillFilterList(filterValues.getFormTypes()));

						getView().createReportPeriodFilter(result.getTaxPeriods());
						getView().setupUI();

						fillDepartmentsMap(result.getDepartments());
						fillFormTypeMap(filterValues.getFormTypes());
						fillReportPeriodsMap(Arrays.asList(result.getCurrentReportPeriod()));
					}
				}));
	}

	public void setSelectedFilterValues(FormDataFilter formDataFilter){
		if(formDataFilter.getFormTypeId() != null){
			getView().setFormTypeValue(formTypeMap.get(formDataFilter.getFormTypeId()));
		}
		if(formDataFilter.getFormDataKind() != null){
			getView().setFormKindValue(formDataFilter.getFormDataKind());
		}
		if(formDataFilter.getDepartmentId().size() == 1){
			Map<String, Integer> value = new HashMap<String, Integer>();
			Integer departmentId = formDataFilter.getDepartmentId().iterator().next();
			String departmentName = departmentMap.get(departmentId).getName();
			value.put(departmentName, departmentId);
			getView().setDepartmentValue(value);
		}
		if(formDataFilter.getReportPeriodIds().size() == 1){
			List<ReportPeriod> value = new ArrayList<ReportPeriod>();
			value.add(reportPeriodMap.get(formDataFilter.getReportPeriodIds().iterator().next()));
			getView().setReportPeriodValue(value);
		}
	}

	private Set<Integer> convertDepartmentsToIds(List<Department> source){
		Set<Integer> result = new HashSet<Integer>();
		for(Department department : source){
			result.add(department.getId());
		}
		return result;
	}

	private <T> List<T> fillFilterList(List<T> source){
		List<T> kind = new ArrayList<T>();
		kind.add(null);
		kind.addAll(source);
		return kind;
	}

	private void fillFormTypeMap(List<FormType> source){
		formTypeMap.clear();
		for(FormType formType : source){
			formTypeMap.put(formType.getId(), formType);
		}
	}

	private void fillDepartmentsMap(List<Department> source){
		departmentMap.clear();
		for(Department department : source){
			departmentMap.put(department.getId(), department);
		}
	}

	private void fillReportPeriodsMap(List<ReportPeriod> source){
		reportPeriodMap.clear();
		for(ReportPeriod reportPeriod : source){
			reportPeriodMap.put(reportPeriod.getId(), reportPeriod);
		}
	}

	private boolean isFilterDataCorrect(FormDataFilter filter){
		if(filter.getReportPeriodIds() == null || filter.getReportPeriodIds().isEmpty()){
			System.out.println(filter.getReportPeriodIds());
			System.out.println(filter.getReportPeriodIds().isEmpty());
			MessageEvent.fire(DialogPresenter.this, "Для создания налоговой формы необходимо выбрать отчетный период");
			return false;
		}
		if(filter.getDepartmentId() == null || filter.getDepartmentId().isEmpty()){
			MessageEvent.fire(DialogPresenter.this, "Для создания налоговой формы необходимо выбрать подразделение");
			return false;
		}
		if(filter.getFormDataKind() == null){
			MessageEvent.fire(DialogPresenter.this, "Для создания налоговой формы необходимо выбрать тип налоговой формы");
			return false;
		}
		if(filter.getFormTypeId() == null){
			MessageEvent.fire(DialogPresenter.this, "Для создания налоговой формы необходимо выбрать вид налоговой формы");
			return false;
		}
		return true;
	}
}
