package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest.Builder;

public class DialogPresenter extends PresenterWidget<DialogPresenter.MyView> implements DialogUiHandlers {

	public static final String NEED_TO_SELECT_FORM_DATA_KIND = "Для создания налоговой формы необходимо выбрать тип налоговой формы";
	public static final String NEED_TO_SELECT_FORM_DATA_TYPE = "Для создания налоговой формы необходимо выбрать вид налоговой формы";
	public static final String NEED_TO_SELECT_DEPARTMENT = "Для создания налоговой формы необходимо выбрать подразделение";
	public static final String NEED_TO_SELECT_REPORT_PERIOD = "Для создания налоговой формы необходимо выбрать отчетный период";

	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;
	
	private final Map<Integer, FormType> formTypeMap = new HashMap<Integer, FormType>();

	public interface MyView extends PopupView, HasUiHandlers<DialogUiHandlers> {
		
		void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues);
		void setAcceptableFormKindList(List<FormDataKind> list);
		void setAcceptableFormTypeList(List<FormType> list);
		void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods);
		
		
		void setFormTypeValue(FormType value);
		void setFormKindValue(FormDataKind value);
		void setDepartmentValue(List<Integer> value);
		void setReportPeriodValue(List<Integer> value);
		
		FormDataFilter getFilterData();
		void clearInput();
		
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
			LogCleanEvent.fire(this);
			LogShowEvent.fire(this, false);
			CreateFormData action = new CreateFormData();
			action.setDepartmentId(filterFormData.getDepartmentId().iterator().next());
			action.setFormDataKindId(filterFormData.getFormDataKind().getId());
			action.setFormDataTypeId(filterFormData.getFormTypeId());
			action.setReportPeriodId(filterFormData.getReportPeriodIds().iterator().next());
			dispatchAsync.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<CreateFormDataResult>() {
						@Override
						public void onSuccess(final CreateFormDataResult createResult) {
							getView().hide();
							getView().clearInput();
							placeManager.revealPlace(new Builder().nameToken(FormDataPresenter.NAME_TOKEN).with(FormDataPresenter.READ_ONLY, "false").with(FormDataPresenter.FORM_DATA_ID, String.valueOf(createResult.getFormDataId())).build());
						}
					}, DialogPresenter.this)
			);
		}

	}


	public void initDialog(TaxType taxType){
		GetFilterData action = new GetFilterData();
		action.setTaxType(taxType);
		dispatchAsync.execute(action, CallbackUtils
				.wrongStateCallback(new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onSuccess(GetFilterDataResult result) {
						FormDataFilterAvailableValues filterValues = result.getFilterValues();
						getView().setAcceptableDepartments(result.getDepartments(), filterValues.getDepartmentIds());
						getView().setAcceptableFormKindList(whithEmptyList(filterValues.getKinds()));
						getView().setAcceptableFormTypeList(whithEmptyList(filterValues.getFormTypes()));
						getView().setAcceptableReportPeriods(result.getReportPeriods());
						fillFormTypeMap(filterValues.getFormTypes());
					}
				}, this));
	}

	public void setSelectedFilterValues(FormDataFilter formDataFilter){
		if(formDataFilter.getFormTypeId() != null){
			getView().setFormTypeValue(formTypeMap.get(formDataFilter.getFormTypeId()));
		}
		if(formDataFilter.getFormDataKind() != null){
			getView().setFormKindValue(formDataFilter.getFormDataKind());
		}
		if(formDataFilter.getDepartmentId()!= null && formDataFilter.getDepartmentId().size() == 1){
			getView().setDepartmentValue(Arrays.asList(formDataFilter.getDepartmentId().get(0)));
		}
		if (formDataFilter.getReportPeriodIds()!=null && formDataFilter.getReportPeriodIds().size() == 1){
			getView().setReportPeriodValue(Arrays.asList(formDataFilter.getReportPeriodIds().get(0)));
		}
	}

	private <T> List<T> whithEmptyList(List<T> source){
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

	private boolean isFilterDataCorrect(FormDataFilter filter){
		if(filter.getReportPeriodIds() == null || filter.getReportPeriodIds().isEmpty()){
			MessageEvent.fire(DialogPresenter.this, NEED_TO_SELECT_REPORT_PERIOD);
			return false;
		}
		if(filter.getDepartmentId() == null || filter.getDepartmentId().isEmpty()){
			MessageEvent.fire(DialogPresenter.this, NEED_TO_SELECT_DEPARTMENT);
			return false;
		}
		if(filter.getFormDataKind() == null){
			MessageEvent.fire(DialogPresenter.this, NEED_TO_SELECT_FORM_DATA_KIND);
			return false;
		}
		if(filter.getFormTypeId() == null){
			MessageEvent.fire(DialogPresenter.this, NEED_TO_SELECT_FORM_DATA_TYPE);
			return false;
		}
		return true;
	}
}
