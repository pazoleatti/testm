package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.*;
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
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest.Builder;

public class CreateFormDataPresenter extends PresenterWidget<CreateFormDataPresenter.MyView> implements CreateFormDataUiHandlers {

	public static final String NEED_TO_SELECT_FORM_DATA_KIND = "Для создания налоговой формы необходимо выбрать тип налоговой формы";
	public static final String NEED_TO_SELECT_FORM_DATA_TYPE = "Для создания налоговой формы необходимо выбрать вид налоговой формы";
	public static final String NEED_TO_SELECT_DEPARTMENT = "Для создания налоговой формы необходимо выбрать подразделение";
	public static final String NEED_TO_SELECT_REPORT_PERIOD = "Для создания налоговой формы необходимо выбрать отчетный период";

	private final PlaceManager placeManager;
	private final DispatchAsync dispatchAsync;

	public interface MyView extends PopupView, HasUiHandlers<CreateFormDataUiHandlers> {
		
		void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues);
		void setAcceptableFormKindList(List<FormDataKind> list);
		void setAcceptableFormTypeList(List<FormType> list);
		void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods);
		
		FormDataFilter getFilterData();
		void setFilterData(FormDataFilter filter);
		
	}

	@Inject
	public CreateFormDataPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
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
			action.setDepartmentId(filterFormData.getDepartmentIds().iterator().next());
			action.setFormDataKindId(filterFormData.getFormDataKind().getId());
			action.setFormDataTypeId(filterFormData.getFormTypeId());
			action.setReportPeriodId(filterFormData.getReportPeriodIds().iterator().next());
			dispatchAsync.execute(action, CallbackUtils
					.defaultCallback(new AbstractCallback<CreateFormDataResult>() {
						@Override
						public void onSuccess(final CreateFormDataResult createResult) {
							getView().hide();
							placeManager.revealPlace(new Builder().nameToken(FormDataPresenter.NAME_TOKEN).with(FormDataPresenter.READ_ONLY, "false").with(FormDataPresenter.FORM_DATA_ID, String.valueOf(createResult.getFormDataId())).build());
						}
					}, CreateFormDataPresenter.this)
			);
		}

	}


	public void initAndShowDialog(final FormDataFilter filter, final HasPopupSlot slotForMe){
		final GetFilterData action = new GetFilterData();
		action.setTaxType(filter.getTaxType());
		dispatchAsync.execute(action, CallbackUtils
				.wrongStateCallback(new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onSuccess(GetFilterDataResult result) {
						FormDataFilterAvailableValues filterValues = result.getFilterValues();
						getView().setAcceptableDepartments(result.getDepartments(), filterValues.getDepartmentIds());
						//TODO Костыли. Связано это с тем, что необходимы разные значения для создания и фильтрации.
						if (action.getTaxType() == TaxType.DEAL) {
							filterValues.getKinds().remove(FormDataKind.SUMMARY);
							filterValues.getKinds().remove(FormDataKind.CONSOLIDATED);
						}
						getView().setAcceptableFormKindList(filterValues.getKinds());
						getView().setAcceptableFormTypeList(filterValues.getFormTypes());
						getView().setAcceptableReportPeriods(result.getReportPeriods());
						
						setSelectedFilterValues(filter);
						slotForMe.addToPopupSlot(CreateFormDataPresenter.this);
					}
				}, this));
	}

	private void setSelectedFilterValues(FormDataFilter formDataFilter){
		FormDataFilter filter = new FormDataFilter();
		if(formDataFilter.getFormTypeId() != null){
			 filter.setFormTypeId(formDataFilter.getFormTypeId());
		}
		if(formDataFilter.getFormDataKind() != null){
			filter.setFormDataKind(formDataFilter.getFormDataKind());
		}
		if(formDataFilter.getDepartmentIds()!= null && formDataFilter.getDepartmentIds().size() == 1){
			filter.setDepartmentIds(formDataFilter.getDepartmentIds());
		}
		if (formDataFilter.getReportPeriodIds()!=null && formDataFilter.getReportPeriodIds().size() == 1){
			filter.setReportPeriodIds(formDataFilter.getReportPeriodIds());
		}
		getView().setFilterData(filter);
	}

	private boolean isFilterDataCorrect(FormDataFilter filter){
		if(filter.getReportPeriodIds() == null || filter.getReportPeriodIds().isEmpty()){
			MessageEvent.fire(CreateFormDataPresenter.this, NEED_TO_SELECT_REPORT_PERIOD);
			return false;
		}
		if(filter.getDepartmentIds() == null || filter.getDepartmentIds().isEmpty()){
			MessageEvent.fire(CreateFormDataPresenter.this, NEED_TO_SELECT_DEPARTMENT);
			return false;
		}
		if(filter.getFormDataKind() == null){
			MessageEvent.fire(CreateFormDataPresenter.this, NEED_TO_SELECT_FORM_DATA_KIND);
			return false;
		}
		if(filter.getFormTypeId() == null){
			MessageEvent.fire(CreateFormDataPresenter.this, NEED_TO_SELECT_FORM_DATA_TYPE);
			return false;
		}
		return true;
	}
}
