package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.SelectDepartmentsEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.popup.SelectDepartmentsEvent;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    // Параметры выбранного подразделения
    private DepartmentCombined data;

    // Выбранное подразделение
    private Integer departmentId;
    private String departmentName;

    @UiField
    TextArea commonName,
            incomeApproveDocName,
            incomeApproveOrgName,
            transportApproveDocName,
            transportApproveOrgName;

    @UiField
    TextBox commonPhone,
            commonInn,
            commonKpp,
            commonTaxOrganCode,
            commonReorgInn,
            commonReorgKpp,
            incomeSignatorySurname,
            incomeSignatoryFirstName,
            incomeSignatoryLastName,
            incomeAppVersion,
            incomeFormatVersion,
            transportSignatorySurname,
            transportSignatoryFirstName,
            transportSignatoryLastName,
            transportAppVersion,
            transportFormatVersion;

    @UiField
    DoubleBox incomeTaxRate;

    @UiField
    LongBox incomeExternalTaxSum,
            incomeSumDifference;

    @UiField
    @Ignore
    VerticalPanel taxTypeIPanel,
            taxTypeTPanel;

    @UiField
    @Ignore
    HorizontalPanel incomeExtTaxSumPanel,
            incomeSumDiffPanel,
            incomePayPanel;

    @UiField
    @Ignore
    Button saveButton,
            cancelButton;

    @UiField
    @Ignore
    ButtonLink editButton;

    @UiField
    @Ignore
    FormPanel formPanel;

    @UiField
    @Ignore
    DepartmentPicker departmentPicker;

    @UiField
    @Ignore
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

                Integer selDepartmentId = event.getItems().values().iterator().next();
                String selDepartmentName = event.getItems().keySet().iterator().next();

                if (driver.isDirty() && isEditMode) {
                    if (!Window.confirm("Все несохранённые данные будут потеряны. Выйти из режима редактирования?")) {
                        // Вернуть старое подразделение
                        departmentPicker.setSelectedItems(new HashMap<String, Integer>() {{
                            put(DepartmentConfigView.this.departmentName, DepartmentConfigView.this.departmentId);
                        }});

                        return;
                    }
                }

                setEditMode(false);

                // Проверка совпадения выбранного подразделения с текущим
                if (DepartmentConfigView.this.departmentId != null && DepartmentConfigView.this.departmentId.equals(selDepartmentId)) {
                    return;
                }

                DepartmentConfigView.this.departmentId = selDepartmentId;
                DepartmentConfigView.this.departmentName = selDepartmentName;

                // Загрузка параметров
                getUiHandlers().updateDepartment(selDepartmentId);
            }
        });

        // Вид налога
        taxType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                // Переключение панелей
                TaxType type = TaxType.fromCode(taxType.getValue(taxType.getSelectedIndex()).charAt(0));
                taxTypeIPanel.setVisible(type == TaxType.INCOME);
                taxTypeTPanel.setVisible(type == TaxType.TRANSPORT);
            }
        });
    }

    @Override
    public void updateVisibility(boolean isUnp) {
        incomeExtTaxSumPanel.setVisible(isUnp);
        incomeSumDiffPanel.setVisible(isUnp);
        incomePayPanel.setVisible(!isUnp);
    }

    @UiHandler("saveButton")
    public void onSave(ClickEvent event) {
        getUiHandlers().save(driver.flush());
        driver.edit(data);
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {

        if (driver.isDirty() && isEditMode) {
            if (!Window.confirm("Все несохранённые данные будут потеряны. Выйти из режима редактирования?")) {
                return;
            }
        }

        setEditMode(false);
        driver.edit(data);
    }

    @UiHandler("editButton")
    public void onEdit(ClickEvent event) {
        setEditMode(true);
    }

    /**
     * Режим редактирования / чтения
     *
     * @param isEditMode Флаг
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
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
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
        this.departmentId = department != null ? department.getId() : null;
        this.departmentName = department != null ? department.getName() : null;
    }

    @Override
    public void setDepartmentCombined(DepartmentCombined combinedDepartmentParam) {
        data = combinedDepartmentParam;
        driver.edit(data);
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
}