package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.PeriodCreated;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckPeriodStatusAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckPeriodStatusResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenPeriodResult;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Диалог открытия периода
 */
public class OpenDialogPresenter extends PresenterWidget<OpenDialogPresenter.MyView> implements OpenDialogUiHandlers {

	public interface MyView extends PopupView, HasUiHandlers<OpenDialogUiHandlers> {
		void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable);
		void setYear(int year);
		void setTaxType(TaxType taxType);
        void setSelectedDepartment(Integer departmentId);
        void resetForm();
		void setCanChangeDepartment(boolean canChange);
	}

	private DispatchAsync dispatcher;
	private TaxType taxType;

	@Inject
	public OpenDialogPresenter(final EventBus eventBus, final MyView view,
	                           DispatchAsync dispatcher, PlaceManager placeManager) {
		super(eventBus, view);
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onHide() {
		getView().hide();
	}

	public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable) {
		getView().setDepartments(departments, avalDepartments, selectedDepartments, enable);
	}

	public void setCanChangeDepartment(boolean canChange) {
		getView().setCanChangeDepartment(canChange);
	}

	public void setYear(int year) {
		getView().setYear(year);
	}

	@Override
	public void onContinue(final OpenFilterData openFilterData) {
        if (openFilterData.getDictionaryTaxPeriod() == null) {
            Dialog.warningMessage("Не заданы все обязательные параметры!");
			return;
		}

		CheckPeriodStatusAction checkPeriodStatusAction = new CheckPeriodStatusAction();
		checkPeriodStatusAction.setYear(openFilterData.getYear());
		checkPeriodStatusAction.setTaxType(this.taxType);
		checkPeriodStatusAction.setDepartmentId(openFilterData.getDepartmentId());
		checkPeriodStatusAction.setDictionaryTaxPeriodId(openFilterData.getDictionaryTaxPeriod());
		checkPeriodStatusAction.setBalancePeriod(openFilterData.isBalancePeriod());
		dispatcher.execute(checkPeriodStatusAction, CallbackUtils
                .defaultCallback(new AbstractCallback<CheckPeriodStatusResult>() {
                    @Override
                    public void onSuccess(CheckPeriodStatusResult result) {
                        switch (result.getStatus()) {
                            case OPEN:
                                Dialog.infoMessage("Периорд уже открыт!");
                                break;
                            case NOT_EXIST:
                                open(openFilterData);
                                break;
                            case CLOSE:
                                Dialog.confirmMessage("Период закрыт, выполнить его переоткрытие?", new DialogHandler() {
                                    @Override
                                    public void yes() {
                                        open(openFilterData);
                                    }

                                    @Override
                                    public void no() {
                                        return;
                                    }

                                    @Override
                                    public void close() {
                                       no();
                                    }
                                });
                                break;
                            case BALANCE_STATUS_CHANGED:
                                Dialog.warningMessage("В Системе может быть заведён только один период с (без) указания признака ввода остатков!");
                                break;
                            default:
                                getView().hide();
                                break;
                        }
                    }
                }, OpenDialogPresenter.this)
        );
	}

	private void open(final OpenFilterData openFilterData) {
		OpenPeriodAction action = new OpenPeriodAction();
		action.setYear(openFilterData.getYear());
		action.setEndDate(openFilterData.getEndDate());
		action.setTaxType(this.taxType);
		action.setDepartmentId(openFilterData.getDepartmentId());
		action.setBalancePeriod(openFilterData.isBalancePeriod());
		action.setDictionaryTaxPeriodId(openFilterData.getDictionaryTaxPeriod());
		action.setHasCorrectPeriod(openFilterData.isHasCorrectPeriod());
		action.setCorrectPeriod(openFilterData.getCorrectPeriod());
		dispatcher.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<OpenPeriodResult>() {
					@Override
					public void onSuccess(OpenPeriodResult result) {
						PeriodCreated.fire(OpenDialogPresenter.this, true, openFilterData.getYear());
						LogAddEvent.fire(OpenDialogPresenter.this, result.getUuid());
						getView().hide();
					}
				}, OpenDialogPresenter.this)
		);
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
		getView().setTaxType(taxType);
	}

    public void setSelectedDepartment(Integer departmentId){
        getView().setSelectedDepartment(departmentId);
    }

	public void resetToDefault() {
        getView().resetForm();
        getView().setYear(Integer.valueOf(DateTimeFormat.getFormat("yyyy").format(new Date())).intValue());
	}
}
