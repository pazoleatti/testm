package com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.PeriodStatusBeforeOpen;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.event.UpdateForm;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.List;
import java.util.Set;

public class EditDialogPresenter extends PresenterWidget<EditDialogPresenter.MyView> implements EditDialogUiHandlers {

	public interface MyView extends PopupView, HasUiHandlers<EditDialogUiHandlers> {
		void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable);
		void setYear(int year);
		void setTaxType(TaxType taxType);
        void setSelectedDepartment(Integer departmentId);
		void setCanChangeDepartment(boolean canChange);
        void setReportPeriod(long reportPeriodId, String periodName);
        void setBalancePeriod(boolean isBalance);
	}

	private DispatchAsync dispatcher;
	private TaxType taxType;
    private EditDialogData initData;

	@Inject
	public EditDialogPresenter(final EventBus eventBus, final MyView view,
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

	@Override
	public void onContinue(final EditDialogData data) {
        if ((data.getYear() == null)
                || (data.getReportPeriodId() == null)
                || (data.getDepartmentId() == null)) {
            Dialog.errorMessage("Редактирование параметров", "Не заполнены следующие обязательные к заполнению поля: "
                    + ((data.getDepartmentId() == null) ? "\"Подразделение\"" : "")
                    + ((data.getDepartmentId() == null) && (data.getYear() == null) ? ", " : "")
                    + ((data.getYear() == null) ? "\"Год\"" : "")
                    + ((data.getDepartmentId() == null) || (data.getYear() == null) && (data.getReportPeriodId() == null) ? ", " : "")
                    + ((data.getReportPeriodId() == null) ? "\"Период\"" : "")
                    + "!"
            );
            return;
        }

        if ((data.isBalance() == initData.isBalance())
                && (data.getYear().equals(initData.getYear()))
                && (data.getReportPeriodId() == initData.getDictTaxPeriodId().intValue())
                && (data.getDepartmentId().equals(initData.getDepartmentId()))) {
            Dialog.errorMessage("Редактирование параметров", "Ни один параметр не был изменен!");
            return;
        }


        CanRemovePeriodAction action = new CanRemovePeriodAction();
        action.setReportPeriodId(initData.getReportPeriodId().intValue());
        action.setOperationName("редактируемом");
        action.setTaxType(taxType);
        action.setDepartmentId(data.getDepartmentId());
        dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<CanRemovePeriodResult>() {
                            @Override
                            public void onSuccess(CanRemovePeriodResult result) {
                                if (!result.isCanRemove()) {
                                    LogAddEvent.fire(EditDialogPresenter.this, result.getUuid());
                                    Dialog.errorMessage("Редактирование периода", "Редактирование периода невозможно!");
                                } else {
                                    checkHasCorrectionPeriod(data);
                                }
                            }
                        }, EditDialogPresenter.this)
        );



	}

    private void checkHasCorrectionPeriod(final EditDialogData data) {
        CheckHasCorrectionPeriodAction hasCorrectionPeriodAction = new CheckHasCorrectionPeriodAction();
        hasCorrectionPeriodAction.setTaxType(taxType);
        hasCorrectionPeriodAction.setDepartmentId(data.getDepartmentId().intValue());
        hasCorrectionPeriodAction.setReportPeriodId(initData.getReportPeriodId() );
        dispatcher.execute(hasCorrectionPeriodAction, CallbackUtils
                        .defaultCallback(new AbstractCallback<CheckHasCorrectionPeriodResult>() {
                            @Override
                            public void onSuccess(CheckHasCorrectionPeriodResult result) {
                                if (result.isHasCorrectionPeriods()) {
                                    Dialog.errorMessage("Редактирование периода", "Перед изменением периода необходимо удалить все связанные корректирующие периоды!");
                                } else {
                                    checkIsPeriodOpen(data);
                                }
                            }
                        }, EditDialogPresenter.this)
        );
    }

    private void checkIsPeriodOpen(final EditDialogData data) {
        IsPeriodOpenAction action = new IsPeriodOpenAction();
        action.setDepartmentId(initData.getDepartmentId());
        action.setReportPeriodId(initData.getReportPeriodId());

        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<IsPeriodOpenResult>() {
                    @Override
                    public void onSuccess(IsPeriodOpenResult result) {
                        if (result.isPeriodOpen()) {
                            checkPeriodStatus(data);
                        } else {
                            Dialog.errorMessage("Редактирование параметров", "Закрытый период не может быть отредактирован!");
                        }
                    }
                }, EditDialogPresenter.this)
        );
    }

    private void checkPeriodStatus(final EditDialogData data) {
        CheckPeriodStatusAction checkPeriodStatusAction = new CheckPeriodStatusAction();
        checkPeriodStatusAction.setBalancePeriod(data.isBalance());
        checkPeriodStatusAction.setDepartmentId(data.getDepartmentId());
        checkPeriodStatusAction.setTaxType(taxType);
        checkPeriodStatusAction.setDictionaryTaxPeriodId(data.getDictTaxPeriodId());
        checkPeriodStatusAction.setYear(data.getYear());


        dispatcher.execute(checkPeriodStatusAction, CallbackUtils
                        .defaultCallback(new AbstractCallback<CheckPeriodStatusResult>() {
                            @Override
                            public void onSuccess(CheckPeriodStatusResult result) {
                                if ((result.getStatus() == PeriodStatusBeforeOpen.OPEN)) {
                                    Dialog.errorMessage("Редактирование периода", "Указанный период уже заведён в Системе!");
                                } else if (PeriodStatusBeforeOpen.BALANCE_STATUS_CHANGED.equals(result.getStatus())) {
                                    if (initData.getDictTaxPeriodId() == data.getReportPeriodId().longValue()) {
                                        edit(data);
                                    } else {
                                        Dialog.errorMessage("Редактирование периода", "Данный период уже заведён в Системе. " +
                                                "Изменение признака ввода остатков невозможно, так как в Системе может быть заведён только один период с (без) указания признака ввода остатков!");
                                    }
                                } else {
                                    edit(data);
                                }
                            }

                        }, EditDialogPresenter.this)
        );
    }

    private void edit(final EditDialogData data) {
        EditPeriodAction action = new EditPeriodAction();
        action.setTaxType(taxType);
        action.setNewDepartmentId(data.getDepartmentId());
        action.setYear(data.getYear());
        action.setBalance(data.isBalance());
        action.setNewDictTaxPeriodId(data.getDictTaxPeriodId().intValue());
        action.setReportPeriodId(initData.getReportPeriodId());
        action.setOldDepartmentId(initData.getDepartmentId());
        dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<EditPeriodResult>() {
                            @Override
                            public void onSuccess(EditPeriodResult result) {
                                LogAddEvent.fire(EditDialogPresenter.this, result.getUuid());
                                getView().hide();
                                UpdateForm.fire(EditDialogPresenter.this);
                            }

                        }, EditDialogPresenter.this)
        );
    }

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
		getView().setTaxType(taxType);
	}

    public void init(EditDialogData data) {
        initData = data;
        getView().setYear(data.getYear());
        getView().setBalancePeriod(data.isBalance());
        getView().setSelectedDepartment(data.getDepartmentId());
        getView().setReportPeriod(data.getDictTaxPeriodId(), data.getPeriodName());
    }

    public void setSelectedDepartment(Integer departmentId){
        getView().setSelectedDepartment(departmentId);
    }
}
