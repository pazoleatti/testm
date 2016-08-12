package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import com.aplana.gwt.client.modal.OpenModalWindowEvent;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateFormDataPresenter extends PresenterWidget<CreateFormDataPresenter.MyView> implements CreateFormDataUiHandlers {
    private final DispatchAsync dispatchAsync;
    private TaxType taxType;
    private CreateFormDataSuccessHandler createFormDataSuccessHandler;

    public interface MyView extends PopupView, HasUiHandlers<CreateFormDataUiHandlers> {
        void init();
        void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues, Integer departmentId);
        void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods, ReportPeriod reportPeriod);
        Integer getDefaultReportPeriodId();

        void addOpenModalWindowHandler(OpenModalWindowEvent.OpenHandler handler);
        void removeOpenModalWindowHandler();

        void setAcceptableMonthList(List<Months> monthList);

        void setAcceptableComparativePeriods(List<ReportPeriod> comparativPeriods);

        void setAcceptableKinds(List<FormDataKind> dataKinds);
        void setAcceptableTypes(List<FormType> types);
        void setCorrectionDate(String correctionDate);
        FormDataFilter getFilterData();
        void setFilterData(FormDataFilter filter);
        void setFilter(String filter);
        void updateLabel();

        void setReportPeriodType(String type);
        /**
         * Устанавливаем в enabled/disabled ежемесячность
         * @param isMonthly true - ежемесячный, false - неежемесячный
         */
        void setFormMonthEnabled(boolean isMonthly);

        void setComparative(boolean comparative);

        void setAccruing(boolean accruing, boolean enabled);

        void setElementNames(Map<FormDataElementName, String> fieldNames);

        void updateEnabled();
    }

    @Inject
    public CreateFormDataPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onHide() {
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
    }

    @Override
    public void onConfirm() {
        FormDataFilter filterFormData = getView().getFilterData();
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        CreateFormData action = new CreateFormData();
        action.setDepartmentId(filterFormData.getDepartmentIds().iterator().next());
        action.setFormDataKindId(filterFormData.getFormDataKind().get(0).intValue());
        action.setFormDataTypeId(filterFormData.getFormTypeId().get(0).intValue());
        action.setReportPeriodId(filterFormData.getReportPeriodIds().iterator().next());
        action.setComparativePeriodId(filterFormData.getComparativePeriodId() != null && !filterFormData.getComparativePeriodId().isEmpty() ? filterFormData.getComparativePeriodId().iterator().next() : null);
        action.setAccruing(filterFormData.isAccruing());
        if (filterFormData.getFormMonth() != null) {
            action.setMonthId(filterFormData.getFormMonth().getId());
        }
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CreateFormDataResult>() {
                    @Override
                    public void onSuccess(final CreateFormDataResult createResult) {
                        getView().hide();
                        // После успешного создания НФ управление переходит к форме списка НФ
                        if (createFormDataSuccessHandler != null) {
                            createFormDataSuccessHandler.onSuccess(createResult);
                        }
                    }
                }, CreateFormDataPresenter.this)
        );
    }

    @Override
    public void onReportPeriodChange() {
        List<Integer> reportIds = getView().getFilterData().getReportPeriodIds();
        if (reportIds == null || reportIds.isEmpty())
            return;

        FillFormFieldsAction action = new FillFormFieldsAction();
        action.setFieldId(reportIds.get(0));
        action.setFieldsNum(FillFormFieldsAction.FieldsNum.SECOND);
        action.setTaxType(taxType);
        dispatchAsync.execute(action, CallbackUtils
                .wrongStateCallback(new AbstractCallback<FillFormFieldsResult>() {
                    @Override
                    public void onSuccess(FillFormFieldsResult result) {
                        getView().setAcceptableDepartments(result.getDepartments(), result.getDepartmentIds(), result.getDefaultDepartmentId());
                        getView().updateEnabled();
                        onDepartmentChanged();
                    }
                }, this));
    }

	@Override
	public void onDepartmentChanged() {
		if ((getView().getFilterData().getDepartmentIds() == null) || getView().getFilterData().getDepartmentIds().isEmpty()) {
			return;
		}
		FillFormFieldsAction action = new FillFormFieldsAction();
		action.setFieldsNum(FillFormFieldsAction.FieldsNum.THIRD);
		action.setTaxType(taxType);
		List<Integer> reportIds = getView().getFilterData().getReportPeriodIds();
		if (reportIds == null || reportIds.isEmpty())
			return;
		action.setFieldId(reportIds.get(0));
		action.setDepartmentId(getView().getFilterData().getDepartmentIds().get(0).longValue());
		action.setReportPeriodId(getView().getFilterData().getReportPeriodIds().get(0));
		dispatchAsync.execute(action, CallbackUtils
				.wrongStateCallback(new AbstractCallback<FillFormFieldsResult>() {
					@Override
					public void onSuccess(FillFormFieldsResult result) {
						getView().setAcceptableKinds(result.getDataKinds());
                        if (result.getCorrectionDate() != null) {
                            getView().setCorrectionDate(DateTimeFormat.getFormat("dd.MM.yyyy").format(result.getCorrectionDate()));
                        } else {
                            getView().setCorrectionDate(null);
                        }
                    }
				}, this));
	}

	@Override
	public void onDataKindChanged() {
		if ((getView().getFilterData().getDepartmentIds() == null) || getView().getFilterData().getDepartmentIds().isEmpty()) {
			return;
		}
		FillFormFieldsAction action = new FillFormFieldsAction();
		action.setFieldsNum(FillFormFieldsAction.FieldsNum.FORTH);
		List<Integer> reportIds = getView().getFilterData().getReportPeriodIds();
		if (reportIds == null || reportIds.isEmpty())
			return;
		action.setFieldId(reportIds.get(0));
		action.setTaxType(taxType);
		action.setDepartmentId(getView().getFilterData().getDepartmentIds().get(0).longValue());
		action.setReportPeriodId(reportIds.get(0));
        List<FormDataKind> kinds = new ArrayList<FormDataKind>();
        for (Long kindId : getView().getFilterData().getFormDataKind()) {
            kinds.add(FormDataKind.fromId(kindId.intValue()));
        }
        action.setKinds(kinds);
		dispatchAsync.execute(action, CallbackUtils
				.wrongStateCallback(new AbstractCallback<FillFormFieldsResult>() {
					@Override
					public void onSuccess(FillFormFieldsResult result) {
						getView().setAcceptableTypes(result.getFormTypes());
					}
				}, this));
	}

    public void initAndShowDialog(final FormDataFilter filter, final HasPopupSlot slotForMe, CreateFormDataSuccessHandler createFormDataSuccessHandler){
        taxType = filter.getTaxType();
        getView().setReportPeriodType(taxType.name());
        this.createFormDataSuccessHandler = createFormDataSuccessHandler;
        FillFormFieldsAction action = new FillFormFieldsAction();
        action.setFieldsNum(FillFormFieldsAction.FieldsNum.FIRST);
        action.setTaxType(taxType);
        action.setReportPeriodId(getView().getDefaultReportPeriodId());
        getView().updateLabel();
        dispatchAsync.execute(action, CallbackUtils
                .wrongStateCallback(new AbstractCallback<FillFormFieldsResult>() {
                    @Override
                    public void onSuccess(FillFormFieldsResult result) {
                        // в текущей постановке фильтры не передаются
                        getView().setFilterData(new FormDataFilter());

                        getView().init();
                        getView().setAcceptableReportPeriods(result.getReportPeriods(), result.getDefaultReportPeriod());
                        getView().setAcceptableDepartments(result.getDepartments(), result.getDepartmentIds(), result.getDefaultDepartmentId());
                        getView().updateEnabled();
                        onDepartmentChanged();
                        changeFilterElementNames(taxType);

                        slotForMe.addToPopupSlot(CreateFormDataPresenter.this);
                    }
                }, this));
    }

    public void changeFilterElementNames(TaxType taxType) {
        GetFieldsNames action = new GetFieldsNames();
        action.setTaxType(taxType);
        dispatchAsync.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<GetFieldsNamesResult>() {
                            @Override
                            public void onSuccess(GetFieldsNamesResult result) {
                                getView().setElementNames(result.getFieldNames());
                            }
                        }, this)
        );
    }

    @Override
    public void checkFormType(Integer formId, Integer reportPeriodId, final Integer comparativePeriodId) {
        GetAdditionalData action = new GetAdditionalData();
        action.setTypeId(formId);
        action.setReportPeriodId(reportPeriodId);
        action.setDepartmentId(getView().getFilterData().getDepartmentIds().get(0));
        action.setTaxType(taxType);
        action.setComparativeReportPeriodId(comparativePeriodId);

        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetAdditionalDataResult>() {
                    @Override
                    public void onSuccess(GetAdditionalDataResult result) {
                        getView().setFormMonthEnabled(result.isMonthly());
                        getView().setComparative(result.isComparative());
                        if (result.isMonthly()) {
                            getView().setAcceptableMonthList(result.getMonthsList());
                        }
                        if (result.isComparative() && comparativePeriodId == null) {
                            getView().setAcceptableComparativePeriods(result.getComparativePeriods());
                            getView().setAccruing(result.isAccruing(), false);
                        } else {
                            getView().setAccruing(result.isAccruing(), !result.isFirstPeriod());
                        }
                        getView().updateEnabled();
                    }
                }, this));
    }

    @Override
    public TaxType getTaxType() {
        return taxType;
    }
}
