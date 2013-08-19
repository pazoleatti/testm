package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.aplana.sbrf.taxaccounting.web.widget.style.ButtonLink;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
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
        implements DepartmentConfigPresenter.MyView, Editor<DepartmentCombined>, ReportPeriodSelectHandler {

    // Признак режима редактирования
    private boolean isEditMode = false;
    // Признак УНП
    private boolean isUnp = false;

    interface Binder extends UiBinder<Widget, DepartmentConfigView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<DepartmentCombined, DepartmentConfigView> {
    }

    private final MyDriver driver = GWT.create(MyDriver.class);

    // Параметры выбранного подразделения
    private DepartmentCombined data;

    // Выбранное подразделение
    private Integer currentDepartmentId;

    // Выбранный период
    private ReportPeriod currentReportPeriod;

    // Выбранный тип налога
    private TaxType currentTaxType = TaxType.INCOME;

    @UiField
    TextBox inn,
            kpp,
            phone,
            taxOrganCode,
            reorgInn,
            reorgKpp,
            signatorySurname,
            signatoryFirstname,
            signatoryLastname,
            appVersion,
            formatVersion;

    @UiField
    TextArea approveDocName,
            approveOrgName;

    @UiField
    DoubleBox taxRate;

    @UiField
    LongBox sumTax,
            sumDividends;

    @UiField
    RefBookPickerPopupWidget
            dictRegionId,
            reorgFormCode,
            signatoryId,
            taxPlaceTypeCode,
            obligation,
            okato,
            okvedCode,
            type;

    @UiField
    TextArea name;

    // Контейнер для справочника периодов
    @UiField
    @Ignore
    SimplePanel reportPeriodPanel;
    ReportPeriodPicker period = new ReportPeriodPicker(this, false);

    @UiField
    @Ignore
    HorizontalPanel sumTaxPanel,
            sumDividendsPanel,
            payPanel,
            taxRatePanel;

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
    DepartmentPickerPopupWidget departmentPicker;

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
        reportPeriodPanel.add(period);
        initListeners();
    }

    private void initListeners() {
        // Подразделение
        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<List<Integer>> event) {

                if (event == null || event.getValue().isEmpty()) {
                    return;
                }

                Integer selDepartmentId = event.getValue().iterator().next();

                boolean checkPass = checkUnsaved(new CheckUnsavedHandler() {
                    @Override
                    public void onCancel() {
                        // Вернуть старое подразделение
                        departmentPicker.setValue(Arrays.asList(DepartmentConfigView.this.currentDepartmentId));
                    }
                });

                if (!checkPass) {
                    return;
                }

                // Проверка совпадения выбранного подразделения с текущим
                if (DepartmentConfigView.this.currentDepartmentId != null
                        && DepartmentConfigView.this.currentDepartmentId.equals(selDepartmentId)) {
                    return;
                }

                DepartmentConfigView.this.currentDepartmentId = selDepartmentId;

                currentReportPeriod = null;

                // Очистка формы
                clear();

                // Обновление налоговых периодов
                reloadTaxPeriods();
			}
		});
        		

        // Вид налога
        taxType.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                boolean checkPass = checkUnsaved(new CheckUnsavedHandler() {
                    @Override
                    public void onCancel() {
                        // Вернуть старое значение
                        for (int i = 0; i < taxType.getItemCount(); i++) {
                            TaxType type = TaxType.fromCode(taxType.getValue(i).charAt(0));
                            if (type == currentTaxType) {
                                taxType.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                });

                if (!checkPass) {
                    return;
                }

                currentTaxType = getSelectedTaxType();

                // Очистка формы
                clear();
                currentReportPeriod = null;
                updateVisibility();

                // Обновление дерева подразделений
                reloadDepartments();

                // Обновление налоговых периодов
                // reloadTaxPeriods();
            }
        });
    }

    private interface CheckUnsavedHandler {
        void onCancel();
    }

    /**
     * Проверка несохраненных изменений
     *
     * @param handler - Handler для обработки отказа
     * @return true - несохраненных изменений нет или пользователь подтвердил выход из режима редактирования
     */
    private boolean checkUnsaved(CheckUnsavedHandler handler) {
        if (isEditMode && driver.isDirty()) {
            if (!Window.confirm("Все несохранённые данные будут потеряны. Выйти из режима редактирования?")) {
                if (handler != null) {
                    handler.onCancel();
                }
                return false;
            }
        }
        setEditMode(false);
        return true;
    }

    @Override
    public void reloadTaxPeriods() {
        getUiHandlers().reloadTaxPeriods(currentTaxType, currentDepartmentId);
    }

    @Override
    public void reloadDepartments() {
        getUiHandlers().reloadDepartments(currentTaxType);
    }

    @Override
    public void setReportPeriod(ReportPeriod reportPeriod) {
        currentReportPeriod = reportPeriod;
        period.setSelectedReportPeriods(Arrays.asList(currentReportPeriod));
        editButton.setVisible(reportPeriod != null && reportPeriod.isActive());
    }

    @Override
    public void reloadDepartmentParams() {
        getUiHandlers().reloadDepartmentParams(currentDepartmentId, currentTaxType,
                currentReportPeriod == null ? null : currentReportPeriod.getId());
    }

    @Override
    public void clear() {
        DepartmentCombined emptyParams = new DepartmentCombined();
        emptyParams.setTaxType(currentTaxType);
        driver.edit(emptyParams);
    }

    /**
     * Выбранный тип налога
     *
     * @return
     */
    private TaxType getSelectedTaxType() {
        TaxType type = null;
        if (taxType.getItemCount() != 0) {
            type = TaxType.fromCode(taxType.getValue(taxType.getSelectedIndex()).charAt(0));
        }
        return type;
    }

    /**
     * Обновление видимости полей
     */
    private void updateVisibility() {
        // Ставка налога
        taxRatePanel.setVisible(currentTaxType == TaxType.INCOME);
        // Сумма налога на прибыль, выплаченная за пределами Российской Федерации в отчётном периоде
        sumTaxPanel.setVisible(isUnp && currentTaxType == TaxType.INCOME);
        // Сумма налога с выплаченных дивидендов за пределами Российской Федерации в последнем квартале отчётного периода
        sumDividendsPanel.setVisible(isUnp && currentTaxType == TaxType.INCOME);
        // Обязанность по уплате налога и Признак расчёта
        payPanel.setVisible(!isUnp && currentTaxType == TaxType.INCOME);
    }

    /**
     * Период по Map'е выбранных значений
     *
     * @param periodsMap
     * @return
     */
    private ReportPeriod getReportPeriod(Map<Integer, ReportPeriod> periodsMap) {
        if (periodsMap != null && periodsMap.size() != 0) {
            return periodsMap.values().iterator().next();
        }
        return null;
    }

    @Override
    public void setUnpFlag(boolean isUnp) {
        this.isUnp = isUnp;
    }

    @UiHandler("saveButton")
    public void onSave(ClickEvent event) {
        getUiHandlers().save(driver.flush(), currentReportPeriod);
        driver.edit(data);
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        if (checkUnsaved(null)) {
            driver.edit(data);
        }
    }

    @UiHandler("editButton")
    public void onEdit(ClickEvent event) {
        if (currentReportPeriod != null && currentReportPeriod.isActive()) {
            setEditMode(true);
        }
    }

    /**
     * Режим редактирования / чтения
     *
     * @param isEditMode Флаг
     */
    private void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        editButton.setVisible(!isEditMode && currentReportPeriod != null && currentReportPeriod.isActive());
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
                if (nextWidget instanceof HasEnabled) {
                    ((HasEnabled) nextWidget).setEnabled(enable);
                }
            }
        }
    }

    @Override
    public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
        getUiHandlers().onTaxPeriodSelected(taxPeriod, currentDepartmentId);
    }

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
        ReportPeriod reportPeriod = getReportPeriod(selectedReportPeriods);

        // Проверка совпадения выбранного подразделения с текущим
        if (this.currentReportPeriod != null && reportPeriod != null && this.currentReportPeriod.getId().equals(reportPeriod.getId())) {
            return;
        }

        boolean checkPass = checkUnsaved(new CheckUnsavedHandler() {
            @Override
            public void onCancel() {
                // Вернуть старое значение
                period.setSelectedReportPeriods(Arrays.asList(DepartmentConfigView.this.currentReportPeriod));
            }
        });

        if (!checkPass) {
            return;
        }

        this.currentReportPeriod = reportPeriod;

        // Редактировать можно только открытые периоды
        editButton.setVisible(reportPeriod != null && reportPeriod.isActive());

        reloadDepartmentParams();
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
        departmentPicker.setAvalibleValues(departments, availableDepartments);
    }

    @Override
    public void setDepartment(final Department department) {
        if (department != null) {
        	departmentPicker.setValue(Arrays.asList(department.getId()), true);
            reloadDepartmentParams();
        }
        
        this.currentDepartmentId = department != null ? department.getId() : null;
    }

    @Override
    public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
        reportPeriodPanel.clear();
        period = new ReportPeriodPicker(this, false);
        period.setTaxPeriods(taxPeriods);
        reportPeriodPanel.add(period);
    }

    @Override
    public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        period.setReportPeriods(reportPeriods);
    }

    @Override
    public void setDepartmentCombined(DepartmentCombined combinedDepartmentParam) {
        data = combinedDepartmentParam;
        driver.edit(data);
    }

    @Override
    public void setDereferenceValue(Map<Long, String> rbTextValues) {
        // Заполнение текстовых значений справочников
        dictRegionId.setDereferenceValue(rbTextValues.get(dictRegionId.getAttributeId()));
        reorgFormCode.setDereferenceValue(rbTextValues.get(reorgFormCode.getAttributeId()));
        signatoryId.setDereferenceValue(rbTextValues.get(signatoryId.getAttributeId()));
        taxPlaceTypeCode.setDereferenceValue(rbTextValues.get(taxPlaceTypeCode.getAttributeId()));
        obligation.setDereferenceValue(rbTextValues.get(obligation.getAttributeId()));
        okato.setDereferenceValue(rbTextValues.get(okato.getAttributeId()));
        okvedCode.setDereferenceValue(rbTextValues.get(okvedCode.getAttributeId()));
        type.setDereferenceValue(rbTextValues.get(type.getAttributeId()));
    }

    @Override
    public void setTaxTypes(List<TaxType> types) {
        taxType.clear();
        if (types != null) {
            for (TaxType type : types) {
                taxType.addItem(type.getName(), String.valueOf(type.getCode()));
            }
        }
        reloadDepartments();
    }
}