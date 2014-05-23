package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client.create;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class CreateBookerStatementsView extends PopupViewWithUiHandlers<CreateBookerStatementsUiHandlers> implements CreateBookerStatementsPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, CreateBookerStatementsView> {
    }

    @UiField
    ModalWindow title;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    @UiField(provided = true)
    ListBoxWithTooltip<BookerStatementsType> bookerReportType;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    @Inject
    public CreateBookerStatementsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);

        bookerReportType = new ListBoxWithTooltip<BookerStatementsType>(new AbstractRenderer<BookerStatementsType>() {
            @Override
            public String render(BookerStatementsType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        bookerReportType.addValueChangeHandler(new ValueChangeHandler<BookerStatementsType>() {
            @Override
            public void onValueChange(ValueChangeEvent<BookerStatementsType> event) {
                updateEnabled();
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void init() {
        reportPeriodIds.setValue(null);
        departmentPicker.setValue(null);
        bookerReportType.setValue(null);
        updateEnabled();
    }

    private void updateEnabled() {
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(reportPeriodIds.getValue() != null && !reportPeriodIds.getValue().isEmpty());
        bookerReportType.setEnabled(departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty());
        // Кнопка "Создать" недоступна пока все не заполнено
        continueButton.setEnabled(bookerReportType.getValue() != null);
    }


    @UiHandler("reportPeriodIds")
    public void onReportPeriodChange(ValueChangeEvent<List<Integer>> event) {
        departmentPicker.setValue(null, true);
        if (getUiHandlers() != null) {
            getUiHandlers().onReportPeriodChange();
        }
        updateEnabled();
    }

    @UiHandler("departmentPicker")
    public void onDepartmentChange(ValueChangeEvent<List<Integer>> event) {
        bookerReportType.setValue(null);
        updateEnabled();
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
    public void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentPicker.setAvalibleValues(list, availableValues);
    }

    @Override
    public void setBookerReportTypes(List<BookerStatementsType> bookerReportTypes) {
        bookerReportType.setAcceptableValues(bookerReportTypes);
    }

    @Override
    public BookerStatementsType getType() {
        return bookerReportType.getValue();
    }

    @Override
    public Integer getDepartment() {
        return departmentPicker.getValue().get(0);
    }

    @Override
    public Integer getReportPeriod() {
        return reportPeriodIds.getValue().get(0);
    }
}
