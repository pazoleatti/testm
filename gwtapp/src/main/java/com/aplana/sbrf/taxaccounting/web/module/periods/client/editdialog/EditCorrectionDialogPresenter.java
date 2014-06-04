package com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class EditCorrectionDialogPresenter extends PresenterWidget<EditCorrectionDialogPresenter.MyView> implements EditCorrectionDialogUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<EditCorrectionDialogUiHandlers> {
        void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable);
        void setTaxType(TaxType taxType);
        void setSelectedDepartment(Integer departmentId);
        void setCanChangeDepartment(boolean canChange);
        void setPeriodsList(List<ReportPeriod> reportPeriods);
    }

    private DispatchAsync dispatcher;
    private TaxType taxType;
    private EditDialogData initData;

    @Inject
    public EditCorrectionDialogPresenter(final EventBus eventBus, final MyView view,
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
        if ((data.getCorrectionDate() == null)
                || (data.getCorrectionReportPeriods() == null)
                || (data.getCorrectionReportPeriods().isEmpty())
                || (data.getCorrectionDate() == null)) {
            Dialog.errorMessage("Редактирование параметров", "Не заполнены следующие обязательные к заполнению поля: "
                            + ((data.getDepartmentId() == null) ? "Подразделение " : "")
                            + ((data.getCorrectionReportPeriods() == null) ? " Период корректировки " : "")
                            + ((data.getCorrectionDate() == null) ? "Период сдачи корректировки " : "")

                            + "!"
            );
            return;
        }

        if (data.getDepartmentId().equals(initData.getDepartmentId())
                && data.getCorrectionDate().equals(initData.getCorrectionDate())
                && data.getCorrectionReportPeriods().get(0).equals(initData.getCorrectionReportPeriods().get(0))) {
            Dialog.errorMessage("Ни одни параметр не был изменен!");
            return;
        }


        CanRemovePeriodAction action = new CanRemovePeriodAction();
        action.setReportPeriodId(initData.getReportPeriodId().intValue());
        dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<CanRemovePeriodResult>() {
                            @Override
                            public void onSuccess(CanRemovePeriodResult result) {
                                if (!result.isCanRemove()) {
                                    Dialog.errorMessage("Редактирование периода невозможно!");
                                } else {
                                    edit(data);
                                }
                            }
                        }, EditCorrectionDialogPresenter.this)
        );
    }

    private void edit(final EditDialogData data) {
        EditPeriodAction action = new EditPeriodAction();
        action.setTaxType(taxType);
        action.setCorrectionDate(initData.getCorrectionDate());
        action.setNewCorrectionDate(data.getCorrectionDate());
        action.setDepartmentId(data.getDepartmentId());
        action.setNewReportPeriodId(data.getReportPeriodId().intValue());
        action.setReportPeriodId(initData.getReportPeriodId().intValue());
        CheckCorrectionPeriodStatusAction checkCorrectionPeriodStatusAction = new CheckCorrectionPeriodStatusAction();
        checkCorrectionPeriodStatusAction.setSelectedDepartments(Arrays.asList(data.getDepartmentId()));
        checkCorrectionPeriodStatusAction.setReportPeriodId(data.getReportPeriodId().intValue());
        checkCorrectionPeriodStatusAction.setTaxType(taxType);
        checkCorrectionPeriodStatusAction.setTerm(data.getCorrectionDate());


        dispatcher.execute(checkCorrectionPeriodStatusAction, CallbackUtils
            .defaultCallback(new AbstractCallback<CheckCorrectionPeriodStatusResult>() {
                @Override
                public void onSuccess(CheckCorrectionPeriodStatusResult result) {
                    if (result.getStatus() == PeriodStatusBeforeOpen.INVALID) {
                        Dialog.errorMessage("Указанный период корректировки должен быть" +
                                " больше последнего корректирующего периода для указанного отчётного периода!");
                    } else if ((result.getStatus() == PeriodStatusBeforeOpen.OPEN)
                            || (result.getStatus() == PeriodStatusBeforeOpen.CLOSE)) {
                        Dialog.errorMessage("Указанный период уже заведён в Системе!");
                    }
                }

            }, EditCorrectionDialogPresenter.this)
        );

    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
        getView().setTaxType(taxType);
    }

    public void init(EditDialogData data) {
        initData = data;
        getView().setSelectedDepartment(data.getDepartmentId());
        getView().setPeriodsList(data.getCorrectionReportPeriods());

    }

    public void setSelectedDepartment(Integer departmentId){
        getView().setSelectedDepartment(departmentId);
    }
}
