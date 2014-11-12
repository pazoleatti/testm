package com.aplana.sbrf.taxaccounting.web.module.ifrs.client.create;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogShowEvent;
import com.aplana.sbrf.taxaccounting.web.module.ifrs.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

public class CreateIfrsDataPresenter extends PresenterWidget<CreateIfrsDataPresenter.MyView> implements CreateIfrsDataUiHandlers {
    private final DispatchAsync dispatchAsync;

    CreateIfrsDataSuccessHandler createIfrsDataSuccessHandler;

    public interface MyView extends PopupView, HasUiHandlers<CreateIfrsDataUiHandlers> {
        void init();
        void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods);
        Integer getReportPeriodId();
    }

    @Inject
    public CreateIfrsDataPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync) {
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
        LogCleanEvent.fire(this);
        LogShowEvent.fire(this, false);
        CreateIfrsDataAction action = new CreateIfrsDataAction();
        action.setReportPeriodId(getView().getReportPeriodId());
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<CreateIfrsDataResult>() {
                    @Override
                    public void onSuccess(final CreateIfrsDataResult createResult) {
                        getView().hide();
                        createIfrsDataSuccessHandler.onSuccess(createResult);
                    }
                }, CreateIfrsDataPresenter.this)
        );
    }


    public void initAndShowDialog(final HasPopupSlot slotForMe, CreateIfrsDataSuccessHandler createIfrsDataSuccessHandler){
        this.createIfrsDataSuccessHandler = createIfrsDataSuccessHandler;
        GetReportPeriodsAction action = new GetReportPeriodsAction();
        dispatchAsync.execute(action, CallbackUtils
                .wrongStateCallback(new AbstractCallback<GetReportPeriodsResult>() {
                    @Override
                    public void onSuccess(GetReportPeriodsResult result) {
                        getView().init();
                        getView().setAcceptableReportPeriods(result.getReportPeriods());

                        slotForMe.addToPopupSlot(CreateIfrsDataPresenter.this);
                    }
                }, this));
    }
}
