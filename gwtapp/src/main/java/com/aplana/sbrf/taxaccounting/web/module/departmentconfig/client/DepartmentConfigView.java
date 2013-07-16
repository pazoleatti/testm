package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.NewDepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.SelectDepartmentsEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.SelectDepartmentsEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.ButtonLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
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

    interface Binder extends UiBinder<Widget, DepartmentConfigView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<DepartmentCombined, DepartmentConfigView> {
    }

    private final MyDriver driver = GWT.create(MyDriver.class);

    @UiField
    @Editor.Ignore
    HorizontalPanel pAll1, pUnp1, pUnp2;

    @UiField
    @Editor.Ignore
    VerticalPanel pAll2;

    @UiField
    @Path("name")
    TextArea depName;

    @UiField
    @Editor.Ignore
    Button saveButton, cancelButton;

    @UiField
    @Editor.Ignore
    ButtonLink editButton;

    @UiField
    @Editor.Ignore
    FormPanel formPanel;

    @UiField
    @Editor.Ignore
    NewDepartmentPicker departmentPicker;

    @Inject
    @UiConstructor
    public DepartmentConfigView(final Binder uiBinder) {

        initWidget(uiBinder.createAndBindUi(this));

        driver.initialize(this);

        enableAllChildren(false, formPanel);
        departmentPicker.setWidth(500);

        departmentPicker.addDepartmentsReceivedEventHandler(new SelectDepartmentsEventHandler() {
            @Override
            public void onDepartmentsReceived(SelectDepartmentsEvent event) {

                if (event == null || event.getItems().isEmpty()) {
                    return;
                }

                getUiHandlers().updateDepartment(event.getItems().values().iterator().next());
            }
        });
    }

    @Override
    public void updateVisibility(boolean isUnp) {
        pAll1.setVisible(!isUnp);
        pAll2.setVisible(!isUnp);
        pUnp1.setVisible(isUnp);
        pUnp2.setVisible(isUnp);
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
                    ((FocusWidget) nextWidget).setEnabled(enable);
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
}