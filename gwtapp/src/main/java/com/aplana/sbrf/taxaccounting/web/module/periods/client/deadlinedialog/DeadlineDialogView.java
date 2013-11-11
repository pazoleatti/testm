package com.aplana.sbrf.taxaccounting.web.module.periods.client.deadlinedialog;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentTreeWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Вью диалогового окна "Установка срока сдачи отчетности"
 * http://conf.aplana.com/pages/viewpage.action?pageId=9598004
 *
 * @author dloshkarev
 */
public class DeadlineDialogView extends PopupViewWithUiHandlers<DeadlineDialogUiHandlers>
        implements DeadlineDialogPresenter.MyView {

    @UiField
    DialogBox deadlineDialog;

    @UiField
    DepartmentTreeWidget departmentPicker;

    @UiField
    CustomDateBox deadline;

    @UiField
    Button saveButton;

    @UiField
    Button cancelButton;

    private static final String DIALOG_TITLE = "Сроки сдачи отчетности для периода: ";

    // Последние выбранные значения на форме. Нужны для восстановления без подгрузки с сервера
    private DepartmentPair lastSelectedDepartment;
    private Date lastSelectedDeadline;

    public interface Binder extends UiBinder<PopupPanel, DeadlineDialogView> {
    }

    @Inject
    public DeadlineDialogView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        initWidget(uiBinder.createAndBindUi(this));

        deadline.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                saveButton.setVisible(true);
                lastSelectedDeadline = event.getValue();
            }
        });

        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<DepartmentPair>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<DepartmentPair>> event) {
                if (event.getValue() != null && !event.getValue().isEmpty()) {
                    DepartmentPair departmentValue = event.getValue().get(0);
                    if (!saveButton.isVisible()) {
                        getUiHandlers().setDepartmentDeadline(departmentValue.getDepartmentId(), departmentValue.getParentDepartmentId());
                        lastSelectedDepartment = departmentValue;
                    } else {
                        if (!lastSelectedDepartment.equals(departmentValue)) {
                            if (Window.confirm("Вы уверены, что хотите прекратить редактирование? Все несохраненные изменения будут потеряны.")) {
                                getUiHandlers().setDepartmentDeadline(departmentValue.getDepartmentId(), departmentValue.getParentDepartmentId());
                                lastSelectedDepartment = departmentValue;
                            } else {
                                departmentPicker.setValue(Arrays.asList(departmentValue));
                                deadline.setValue(lastSelectedDeadline);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void setDepartments(List<Department> departments, List<DepartmentPair> selectedDepartments) {
        departmentPicker.setAvailableValues(departments);
        departmentPicker.setValue(selectedDepartments);
        saveButton.setVisible(false);
        lastSelectedDepartment = selectedDepartments.get(0);
    }

    @Override
    public void setTitle(String periodName, int year) {
        deadlineDialog.setText(DIALOG_TITLE + periodName + " " + year + " года");
    }

    @Override
    public void setDeadLine(Date deadline) {
        this.deadline.setValue(deadline);
    }

    @Override
    public void hideButtons() {
        saveButton.setVisible(false);
    }

    @UiHandler("saveButton")
    public void onSave(ClickEvent event) {
        if (departmentPicker.isSelectedItemHasChildren()) {
            if (Window.confirm("Назначить нижестоящим подразделениям?")) {
                getUiHandlers().updateDepartmentDeadline(departmentPicker.getSelectedChildren(), deadline.getValue(), true);
            } else {
                getUiHandlers().updateDepartmentDeadline(departmentPicker.getValue(), deadline.getValue(), false);
            }
        } else {
            getUiHandlers().updateDepartmentDeadline(departmentPicker.getValue(), deadline.getValue(), false);
        }
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        if (!saveButton.isVisible() || Window.confirm("Вы уверены, что хотите отменить изменения?")) {
            hide();
        }
    }
}
