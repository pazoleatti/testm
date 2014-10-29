package com.aplana.sbrf.taxaccounting.web.module.ifrs.client.create;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.List;

public class CreateIfrsDataView extends PopupViewWithUiHandlers<CreateIfrsDataUiHandlers> implements CreateIfrsDataPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, CreateIfrsDataView> {
    }


    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    @Inject
    public CreateIfrsDataView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void init() {
        reportPeriodIds.setValue(null);
    }

    @UiHandler("continueButton")
    public void onSave(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onConfirm();
        }
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        Dialog.confirmMessage("Отмена создания", "Отменить создание?", new DialogHandler() {
            @Override
            public void yes() {
                Dialog.hideMessage();
                hide();
            }
        });
    }

    @Override
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
    }

    @Override
    public Integer getReportPeriodId() {
        List<Integer> value = reportPeriodIds.getValue();
        if (value != null && value.size() == 1)
            return value.get(0);
        return null;
    }
}
