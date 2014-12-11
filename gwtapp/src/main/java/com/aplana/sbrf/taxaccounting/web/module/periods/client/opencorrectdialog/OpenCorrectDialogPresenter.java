package com.aplana.sbrf.taxaccounting.web.module.periods.client.opencorrectdialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.gwt.client.mask.parser.DMDateParser;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.PeriodCreated;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.*;


/**
 * Диалог открытия периода
 */
public class OpenCorrectDialogPresenter extends PresenterWidget<OpenCorrectDialogPresenter.MyView> implements OpenCorrectDialogUiHandlers {

	public interface MyView extends PopupView, HasUiHandlers<OpenCorrectDialogUiHandlers> {
		void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable);
        void setPeriodsList(List<ReportPeriod> reportPeriods, Integer reportPeriodId);
		void setTaxType(TaxType taxType);
        void setSelectedDepartment(Integer departmentId);
        void resetForm();
		void setCanChangeDepartment(boolean canChange);

        List<Integer> getSelectedDepartments();
        Date getTerm();
        void setTerm(Date term);
        ReportPeriod getSelectedPeriod();
        boolean canChangeDepartment();
	}

	private DispatchAsync dispatcher;
	private TaxType taxType;
    private Boolean balance;

	@Inject
	public OpenCorrectDialogPresenter(final EventBus eventBus, final MyView view,
                                      DispatchAsync dispatcher) {
		super(eventBus, view);
		this.dispatcher = dispatcher;
		getView().setUiHandlers(this);
    }

	@Override
	protected void onHide() {
		getView().hide();
	}

	public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable) {
        List<DepartmentPair> selDep = new ArrayList<DepartmentPair>();
        if (selectedDepartments != null) {
            selDep.addAll(selectedDepartments);
        }
        if (enable) { // Убираем возможность выбирать банк
            if (avalDepartments.remove(0)) {

                for (Iterator<DepartmentPair> it = selDep.iterator(); it.hasNext(); ) {
                    if (it.next().getDepartmentId() == 0) {
                        it.remove();
                    }
                }

                Department firstDep = departments.get(0);
                for (Department dep : departments) {
                    if (dep.getId() != 0) {
                        firstDep = dep;
                        break;
                    }
                }
                DepartmentPair selectedDep = new DepartmentPair(firstDep.getId(), firstDep.getParentId(), firstDep.getName());
                selDep.add(selectedDep);
            }
        }

        getView().setDepartments(departments, avalDepartments, selDep, enable);
	}

	public void setCanChangeDepartment(boolean canChange) {
		getView().setCanChangeDepartment(canChange);
	}

	public void setPeriodsList(List<ReportPeriod> reportPeriods, Integer reportPeriodId) {
        getView().setPeriodsList(reportPeriods, reportPeriodId);
	}

	@Override
	public void onContinue() {
        if(balance != null && balance){
            Dialog.errorMessage("Ошибка", "Корректирующий период не может быть открыт для периода ввода остатков!");
            return;
        }
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
            Dialog.errorMessage("Ошибка", missedFields.toString());
            return;
        }

        if (getView().getSelectedPeriod().getCalendarStartDate().getYear() > getView().getTerm().getYear()) {
            Dialog.errorMessage("Календарный год периода сдачи корректировки не должен быть меньше календарного года корректируемого периода!");
            return;
        }

        CheckCorrectionPeriodStatusAction action = new CheckCorrectionPeriodStatusAction();
        action.setSelectedDepartments(getView().getSelectedDepartments());
        action.setReportPeriodId(getView().getSelectedPeriod().getId());
        action.setTerm(getView().getTerm());

        dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<CheckCorrectionPeriodStatusResult>() {
                            @Override
                            public void onSuccess(CheckCorrectionPeriodStatusResult result) {
                                switch (result.getStatus()) {
                                    case CLOSE_AND_BALANCE:
                                        Dialog.errorMessage("Корректирование периода", "Корректирующий период не может быть открыт для периода ввода остатков!");
                                        break;
                                    case NOT_EXIST:
                                        openCorrectionPeriod();
                                        break;
                                    case CLOSE:
                                        Dialog.confirmMessage("Корректирование периода", "Корректирующий период с датой корректировки " +
                                                DMDateParser.formatDMY.format(getView().getTerm()) + " закрыт, выполнить переоткрытие?",
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
                                    case OPEN:
                                        Dialog.errorMessage("Корректирование периода",
                                                "Корректирующий период с датой корректировки " +
                                                        DMDateParser.formatDMY.format(getView().getTerm()) + " уже открыт!");
                                        break;
                                    case INVALID:
                                        Dialog.errorMessage("Корректирование периода",
                                                "Корректирующий период с датой корректировки " +
                                                        DMDateParser.formatDMY.format(getView().getTerm()) +
                                                        " не может быть открыт, т.к. существует более поздний корректирующий период!");
                                        break;
                                    case CORRECTION_PERIOD_LAST_OPEN:
                                        Dialog.errorMessage( "Корректирование периода", "Корректирующий период с датой корректировки " +
                                                DMDateParser.formatDMY.format(getView().getTerm()) +
                                                " не может быть открыт, т.к. открыт более ранний корректирующий период!");
                                        break;
                                    case CORRECTION_PERIOD_NOT_CLOSE:
                                        Dialog.errorMessage( "Корректирование периода", "Корректирующий период с датой корректировки " +
                                                DMDateParser.formatDMY.format(getView().getTerm()) + " не может быть открыт, т.к. период корректировки не закрыт!");
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
                                PeriodCreated.fire(OpenCorrectDialogPresenter.this, true,
                                        getView().getSelectedPeriod().getStartDate().getYear()+1900, getView().getSelectedDepartments().get(0));
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

    private void setSelectedDepartment(Integer departmentId){
        if (departmentId != 0 && getView().canChangeDepartment()) {
            getView().setSelectedDepartment(departmentId);
        }
    }

    public void init(TableRow data){
        setSelectedDepartment(data.getDepartmentId());
        balance = data.isBalance();
        getView().setTerm(new Date());
    }

	public void resetToDefault() {
        getView().resetForm();
	}
}
