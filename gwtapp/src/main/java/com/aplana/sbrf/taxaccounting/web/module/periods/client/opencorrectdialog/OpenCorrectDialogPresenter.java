package com.aplana.sbrf.taxaccounting.web.module.periods.client.opencorrectdialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.PeriodCreated;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckCorrectionPeriodStatusAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.CheckCorrectionPeriodStatusResult;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenCorrectPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.OpenCorrectPeriodResult;
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
public class OpenCorrectDialogPresenter extends PresenterWidget<OpenCorrectDialogPresenter.MyView> implements OpenCorrectDialogUiHandlers {

	public interface MyView extends PopupView, HasUiHandlers<OpenCorrectDialogUiHandlers> {
		void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable);
        void setPeriodsList(List<ReportPeriod> reportPeriods);
		void setTaxType(TaxType taxType);
        void setSelectedDepartment(Integer departmentId);
        void resetForm();
		void setCanChangeDepartment(boolean canChange);

        List<Integer> getSelectedDepartments();
        Date getTerm();
        ReportPeriod getSelectedPeriod();
	}

	private DispatchAsync dispatcher;
	private TaxType taxType;

	@Inject
	public OpenCorrectDialogPresenter(final EventBus eventBus, final MyView view,
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

	public void setPeriodsList(List<ReportPeriod> reportPeriods) {
        getView().setPeriodsList(reportPeriods);

	}

	@Override
	public void onContinue() {
        StringBuilder missedFields = new StringBuilder();
        if (getView().getSelectedDepartments().isEmpty()) {
            missedFields.append(" \"Подразделение\"");
            if (getView().getSelectedPeriod() != null || getView().getTerm() != null) {
                missedFields.append(".");
            }
        }
        if (getView().getSelectedPeriod() == null) {
            if (missedFields.length() != 0) {
                missedFields.append(",");
            }
            missedFields.append(" \"Период корректировки\"");
            if (getView().getTerm() != null) {
                missedFields.append(".");
            }
        }
        if (getView().getTerm() == null) {
            if (missedFields.length() != 0) {
                missedFields.append(",");
            }
            missedFields.append(" \"Период сдачи корректировки\".");
        }

        if (missedFields.length() != 0) {
            missedFields.insert(0, "Не заполнены следующие обязательные к заполнению поля:");
            Dialog.errorMessage(missedFields.toString());
            return;
        }

        if (getView().getSelectedPeriod().getCalendarStartDate().before(getView().getTerm())) {
            Dialog.errorMessage("Календарный год периода сдачи корректировки не должен быть меньше календарного года корректируемого периода!");
            return;
        }

        CheckCorrectionPeriodStatusAction action = new CheckCorrectionPeriodStatusAction();
        action.setSelectedDepartments(getView().getSelectedDepartments());
        action.setSelectedPeriod(getView().getSelectedPeriod());
        action.setTerm(getView().getTerm());

        dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<CheckCorrectionPeriodStatusResult>() {
                            @Override
                            public void onSuccess(CheckCorrectionPeriodStatusResult result) {
                                System.out.println("T: " + result.getStatus());
                                switch (result.getStatus()) {
                                    case NOT_EXIST:
                                        openCorrectionPeriod();
                                        break;
                                    case CLOSE:
                                        Dialog.confirmMessage("Корректирующий период закрыт, выполнить переоткрытие?",
                                                new DialogHandler() {
                                                    @Override
                                                    public void yes() {
                                                        openCorrectionPeriod();
                                                    }

                                                    @Override
                                                    public void no() {
                                                        close();
                                                    }
                                                }
                                        );
                                        break;
                                    case INVALID:
                                        Dialog.errorMessage("Указанный период корректировки должен быть больше " +
                                                "последнего корректирующего периода для указанного отчётного периода!");
                                        break;
                                }
                            }
                        }, OpenCorrectDialogPresenter.this)
        );
	}

    private void openCorrectionPeriod() {
        OpenCorrectPeriodAction action = new OpenCorrectPeriodAction();
        action.setSelectedDepartments(getView().getSelectedDepartments());
        action.setSelectedPeriod(getView().getSelectedPeriod());
        action.setTerm(getView().getTerm());
        action.setTaxType(taxType);

        dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<OpenCorrectPeriodResult>() {
                            @Override
                            public void onSuccess(OpenCorrectPeriodResult result) {
                                PeriodCreated.fire(OpenCorrectDialogPresenter.this, true, getView().getTerm().getYear());
                                LogAddEvent.fire(OpenCorrectDialogPresenter.this, result.getUuid());
                                getView().hide();
                            }
                        }, OpenCorrectDialogPresenter.this)
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
	}
}
