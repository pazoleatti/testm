package com.aplana.sbrf.taxaccounting.web.module.periods.client.deadlinedialog;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.MessageEvent;
import com.aplana.sbrf.taxaccounting.web.module.periods.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import java.util.Date;
import java.util.List;

/**
 * Презентер для диалогового окна "Установка срока сдачи отчетности"
 *
 * @author dloshkarev
 */
public class DeadlineDialogPresenter extends PresenterWidget<DeadlineDialogPresenter.MyView> implements DeadlineDialogUiHandlers {

    public interface MyView extends PopupView, HasUiHandlers<DeadlineDialogUiHandlers> {
        void setDepartments(List<Department> departments, List<DepartmentPair> selectedDepartments);

        void setTitle(String periodName, int year);

        void setDeadLine(Date deadline);

        void hideButtons();
    }

    private DispatchAsync dispatcher;
    private PlaceManager placeManager;
    private String periodName;
    private int year;
    private TaxType taxType;

    private TableRow selectedPeriod;

    @Inject
    public DeadlineDialogPresenter(final EventBus eventBus, final MyView view,
                                   DispatchAsync dispatcher, PlaceManager placeManager) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        this.placeManager = placeManager;
        getView().setUiHandlers(this);
    }

    @Override
    public void setDepartments(List<Department> departments, List<DepartmentPair> selectedDepartments) {
        getView().setDepartments(departments, selectedDepartments);
    }

    @Override
    public void setTitle(String periodName, int year) {
        this.periodName = periodName;
        this.year = year;
        getView().setTitle(periodName, year);
    }

    @Override
    public void setDeadLine(Date deadline) {
        getView().setDeadLine(deadline);
    }

    @Override
    public void setSelectedPeriod(TableRow selectedPeriod) {
        this.selectedPeriod = selectedPeriod;
    }

    @Override
    public void setDepartmentDeadline(int senderDepartmentId, Integer receiverDepartmentId) {
        GetDepartmentDeadlineAction action = new GetDepartmentDeadlineAction();
        action.setReportPeriodId((int) selectedPeriod.getReportPeriodId());
        action.setReceiverDepartmentId(receiverDepartmentId);
        action.setSenderDepartmentId(senderDepartmentId);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetDepartmentDeadlineResult>() {
                    @Override
                    public void onSuccess(GetDepartmentDeadlineResult result) {
                        getView().setDeadLine(result.getDeadline());
                    }
                }, DeadlineDialogPresenter.this)
        );
    }

    @Override
    public void updateDepartmentDeadline(List<DepartmentPair> departments, Date deadline, Boolean isNeedUpdateChildren) {
        if (departments == null || departments.isEmpty()) {
            MessageEvent.fire(this, "Не выбрано подразделение");
            return;
        }

        if (deadline == null) {
            MessageEvent.fire(this, "Не указан срок сдачи отчетности");
            return;
        }

        UpdateDepartmentDeadlineAction action = new UpdateDepartmentDeadlineAction();
        action.setReportPeriodId((int) selectedPeriod.getReportPeriodId());
        action.setDepartments(departments);
        action.setDeadline(deadline);
        action.setCurrentYear(year);
        action.setReportPeriodName(periodName);
        action.setTaxType(taxType);
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<UpdateDepartmentDeadlineResult>() {
                    @Override
                    public void onSuccess(UpdateDepartmentDeadlineResult result) {
                        getView().hideButtons();
                    }
                }, DeadlineDialogPresenter.this)
        );
    }

    @Override
    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}
