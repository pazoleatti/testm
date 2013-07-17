package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.NewDepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.SelectDepartmentsEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.SelectDepartmentsEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.ButtonLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * View для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 */
public class DepartmentConfigView extends ViewWithUiHandlers<DepartmentConfigUiHandlers>
        implements DepartmentConfigPresenter.MyView, Editor<DepartmentCombined> {

    // Признак режима редактирования
    private boolean isEditMode;

    interface Binder extends UiBinder<Widget, DepartmentConfigView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<DepartmentCombined, DepartmentConfigView> {
    }

    private final MyDriver driver = GWT.create(MyDriver.class);

    // Выбранный тип налога
    private int taxTypeIndex = 1;

    @UiField
    TextArea commonName;

    @UiField
    TextBox commonPhone,
            commonInn,
            commonKpp,
            commonTaxOrganCode,
            commonReorgInn,
            commonReorgKpp;

    @UiField
    @Editor.Ignore
    Button saveButton,
           cancelButton;

    @UiField
    @Editor.Ignore
    ButtonLink editButton;

    @UiField
    @Editor.Ignore
    FormPanel formPanel;

    @UiField
    @Editor.Ignore
    NewDepartmentPicker departmentPicker;

    @UiField
    @Editor.Ignore
    ListBox taxType;

    @Inject
    @UiConstructor
    public DepartmentConfigView(final Binder uiBinder) {

        initWidget(uiBinder.createAndBindUi(this));

        driver.initialize(this);

        enableAllChildren(false, formPanel);
        departmentPicker.setWidth(500);

        initListeners();
    }

    private void initListeners() {
        // Подразделение
        departmentPicker.addDepartmentsReceivedEventHandler(new SelectDepartmentsEventHandler() {
            @Override
            public void onDepartmentsReceived(SelectDepartmentsEvent event) {

                if (event == null || event.getItems().isEmpty()) {
                    return;
                }

                getUiHandlers().updateDepartment(event.getItems().values().iterator().next());
            }
        });

        // Вид налога
        taxType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                int index = taxType.getSelectedIndex();
                if (getUiHandlers().onTaxTypeChange(index == -1 ? null : TaxType.fromCode(taxType.getValue(index).charAt(0)))) {
                    // Смена типа
                    taxTypeIndex = index;
                } else {
                    // Откат смены
                    event.stopPropagation();
                    taxType.setSelectedIndex(taxTypeIndex);
                }
            }
        });
    }

    @Override
    public void updateVisibility(boolean isUnp) {
        // TODO обновление видимости полей
    }

    @UiHandler("saveButton")
    public void onSave(ClickEvent event) {
        // driver.flush();
        // TODO Сохранение
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        // TODO Отмена и переход в режим чтения
        setEditMode(false);
    }

    @UiHandler("editButton")
    public void onEdit(ClickEvent event) {
        setEditMode(true);
    }

    /**
     * Режим редактирования / чтения
     *
     * @param isEditMode
     */
    private void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        editButton.setVisible(!isEditMode);
        saveButton.setVisible(isEditMode);
        cancelButton.setVisible(isEditMode);
        enableAllChildren(isEditMode, formPanel);
    }

    /**
     * Рекурсивная установка доступности элементов
     *
     * @param enable Доступность
     * @param widget Корневой виджет
     */
    private void enableAllChildren(boolean enable, Widget widget) {
        if (widget instanceof HasWidgets) {
            Iterator<Widget> iter = ((HasWidgets) widget).iterator();
            while (iter.hasNext()) {
                Widget nextWidget = iter.next();
                enableAllChildren(enable, nextWidget);
                if (nextWidget instanceof FocusWidget) {
                    // Пропускаем "Вид налога", т.к. это не поле ввода, а лишь селектор
                    if (nextWidget != taxType) {
                        ((FocusWidget) nextWidget).setEnabled(enable);
                    }
                }
            }
        }
    }

    @Override
    public void setDepartments(List<Department> departments) {
        Set<Integer> availableDepartments = new HashSet<Integer>(departments.size());
        for (Department department : departments) {
            // TODO убрать недоступные
            availableDepartments.add(department.getId());
        }
        departmentPicker.setTreeValues(departments, availableDepartments);
    }

    @Override
    public void setDepartment(final Department department) {
        if (department != null) {
            departmentPicker.setSelectedItems(new HashMap<String, Integer>() {{
                put(department.getName(), department.getId());
                getUiHandlers().updateDepartment(department.getId());
            }});
        }
    }

    @Override
    public void setDepartmentCombined(DepartmentCombined combinedDepartmentParam) {
        driver.edit(combinedDepartmentParam);
    }

    @Override
    public void setTaxTypes(List<TaxType> types) {
        taxType.clear();
        if (types != null) {
            for (TaxType type : types) {
                taxType.addItem(type.getName(), String.valueOf(type.getCode()));
            }
        }
    }

    @Override
    public boolean isEditMode() {
        return isEditMode;
    }

    @Override
    public boolean isDirty() {
        return driver.isDirty();
    }
}