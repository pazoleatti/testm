package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client.create;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared.*;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.client.BookerStatementsDataTokens;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest.Builder;

import java.util.*;


public class CreateBookerStatementsPresenter extends PresenterWidget<CreateBookerStatementsPresenter.MyView> implements CreateBookerStatementsUiHandlers {
    private final PlaceManager placeManager;
    private final DispatchAsync dispatchAsync;

    public interface MyView extends PopupView, HasUiHandlers<CreateBookerStatementsUiHandlers> {
        void init();
        void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues);
        void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods);
        void setBookerReportTypes(List<BookerStatementsType> bookerReportTypes);
        BookerStatementsType getType();
        Integer getReportPeriod();
        Integer getDepartment();
    }

    @Inject
    public CreateBookerStatementsPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    public void onConfirm() {
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        final CreateBookerStatementsAction action = new CreateBookerStatementsAction();
        action.setDepartmentId(getView().getDepartment());
        action.setBookerStatementsTypeId(getView().getType().getId());
        action.setReportPeriodId(getView().getReportPeriod());

        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CreateBookerStatementsResult>() {
                    @Override
                    public void onSuccess(final CreateBookerStatementsResult result) {
                        LogAddEvent.fire(CreateBookerStatementsPresenter.this, result.getUuid());
                        if (!result.isHasError()) {
                            getView().hide();
                            placeManager.revealPlace(new Builder().nameToken(BookerStatementsDataTokens.bookerStatements)
                                    .with(BookerStatementsDataTokens.DEPARTMENT_ID, action.getDepartmentId().toString())
                                    .with(BookerStatementsDataTokens.REPORT_PERIOD_ID, action.getReportPeriodId().toString())
                                    .with(BookerStatementsDataTokens.TYPE_ID, action.getBookerStatementsTypeId().toString())
                                    .build());
                        } else {
                            Dialog.errorMessage("Создание бухгалтерской отчётности", "Бухгалтерская отчётность не создана!");
                        }
                    }
                }, CreateBookerStatementsPresenter.this)
        );
    }

    @Override
    public void onReportPeriodChange() {
        Integer reportId = getView().getReportPeriod();
        if (reportId == null)
            return;

        BookerStatementsFieldsAction action = new BookerStatementsFieldsAction();
        action.setFieldId(reportId);
        action.setFieldsNum(BookerStatementsFieldsAction.FieldsNum.SECOND);
        dispatchAsync.execute(action, CallbackUtils
                .wrongStateCallback(new AbstractCallback<BookerStatementsFieldsResult>() {
                    @Override
                    public void onSuccess(BookerStatementsFieldsResult result) {
                        getView().setAcceptableDepartments(result.getDepartments(), result.getDepartmentIds());
                    }
                }, this));
    }

    public void initAndShowDialog(final HasPopupSlot slotForMe){
        getView().init();
        slotForMe.addToPopupSlot(CreateBookerStatementsPresenter.this);

        BookerStatementsFieldsAction action = new BookerStatementsFieldsAction();
        action.setFieldsNum(BookerStatementsFieldsAction.FieldsNum.FIRST);

        dispatchAsync.execute(action, CallbackUtils
                .wrongStateCallback(new AbstractCallback<BookerStatementsFieldsResult>() {
                    @Override
                    public void onSuccess(BookerStatementsFieldsResult result) {
                        getView().setAcceptableReportPeriods(result.getReportPeriods());
                        getView().setBookerReportTypes(Arrays.asList(BookerStatementsType.values()));

                        slotForMe.addToPopupSlot(CreateBookerStatementsPresenter.this);
                    }
                }, this));
    }

}
