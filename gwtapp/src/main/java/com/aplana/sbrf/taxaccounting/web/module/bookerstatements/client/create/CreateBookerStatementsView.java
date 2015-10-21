package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client.create;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.Spinner;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.gwt.client.modal.CanHide;
import com.aplana.gwt.client.modal.OnHideHandler;
import com.aplana.sbrf.taxaccounting.model.BookerStatementsType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class CreateBookerStatementsView extends PopupViewWithUiHandlers<CreateBookerStatementsUiHandlers> implements CreateBookerStatementsPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, CreateBookerStatementsView> {
    }

    @UiField
    ModalWindow title;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    Spinner yearBox;

    @UiField
    RefBookPickerWidget accountPeriodIds;

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

        initWidget(uiBinder.createAndBindUi(this));

        Date current = new Date();
        accountPeriodIds.setPeriodDates(current, current);

        title.setOnHideHandler(new OnHideHandler<CanHide>() {
            @Override
            public void onHide(CanHide modalWindow) {
                showOnHideDialog();
            }
        });
    }

    @Override
    public void init() {
        yearBox.setValue(null);
        accountPeriodIds.setValue(null);
        departmentPicker.setValue(null);
        bookerReportType.setValue(null);
    }

    @UiHandler("continueButton")
    public void onSave(ClickEvent event) {
        // Проверка заполненности полей
        String errorString = getEmptyFieldsNames();
        if (!errorString.isEmpty()) {
            Dialog.errorMessage("Указание параметров",
                    "Не заполнены следующие обязательные к заполнению поля: " + errorString + "!");
        } else if (getUiHandlers() != null) {
            getUiHandlers().onConfirm();
        }
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        showOnHideDialog();
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
    public Long getAccountPeriod() {
        return accountPeriodIds.getValue().get(0);
    }

    @Override
    public void setYear(int year) {
        yearBox.setValue(year);
    }

    @Override
    public Integer getYear() {
        return yearBox.getValue();
    }

    private String getEmptyFieldsNames() {
        StringBuilder names = new StringBuilder();
        if (yearBox.getValue() == null) {
            names.append("«Год», ");
        }
        if (accountPeriodIds.getValue() == null || accountPeriodIds.getValue().isEmpty()) {
            names.append("«Период», ");
        }
        if (departmentPicker.getValue() == null || departmentPicker.getValue().isEmpty()) {
            names.append("«Подразделение», ");
        }
        if (bookerReportType.getValue() == null) {
            names.append("«Вид бух. отчетности», ");
        }
        String retNames = names.toString();
        if (retNames.length() > 0) {
            retNames = retNames.substring(0, retNames.length() - 2);
        }
        return retNames;
    }

    private void showOnHideDialog() {
        Dialog.confirmMessage("Отмена создания бухгалтерской отчётности", "Отменить создание бухгалтерской отчётности?", new DialogHandler() {
            @Override
            public void yes() {
                Dialog.hideMessage();
                hide();
            }
        });
    }
}
