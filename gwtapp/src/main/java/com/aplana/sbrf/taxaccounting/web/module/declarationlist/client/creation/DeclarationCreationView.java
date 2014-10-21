package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import com.aplana.gwt.client.ListBoxWithTooltipWidget;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;


public class DeclarationCreationView extends PopupViewWithUiHandlers<DeclarationCreationUiHandlers>
        implements DeclarationCreationPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, DeclarationCreationView> {
    }

    public static final String DECLARATION_TITLE = "Создание декларации";
    public static final String DECLARATION_TITLE_D = "Создание уведомления";
    public static final String DECLARATION_TYPE_TITLE = "Вид декларации:";
    public static final String DECLARATION_TYPE_TITLE_D = "Вид:";
    public static final String DECLARATION_CORRECTION = "Форма будет создана в корректирующем периоде, дата сдачи корректировки: ";
    public static final String NOTIFICATION_CORRECTION = "Уведомление будет создано в корректирующем периоде, дата сдачи корректировки: ";

    @UiField
    ModalWindow modalWindowTitle;

    @UiField
    PeriodPickerPopupWidget periodPicker;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    Label declarationTypeLabel;

    @UiField(provided = true)
    ListBoxWithTooltipWidget<Integer> declarationTypeBox;

    @UiField
    TextBox taxOrganCode;
    @UiField
    TextBox taxOrganKpp;
    @UiField
    HorizontalPanel codePanel;
    @UiField
    HorizontalPanel kppPanel;
    @UiField
    HorizontalPanel correctionPanel;
    @UiField
    Label correctionDate;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;


    final private Map<Integer, DeclarationType> declarationTypesMap = new LinkedHashMap<Integer, DeclarationType>();

    @Inject
    public DeclarationCreationView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);

        declarationTypeBox = new ListBoxWithTooltipWidget<Integer>(new AbstractRenderer<Integer>() {

            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                DeclarationType declarationType = declarationTypesMap.get(object);
                if (declarationType != null) {
                    return declarationType.getName();
                } else {
                    return String.valueOf(object);
                }
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
        init();
    }

    @Override
    public void init() {
        departmentPicker.setEnabled(false);
        declarationTypeBox.setEnabled(false);
        taxOrganCode.setEnabled(false);
        taxOrganKpp.setEnabled(false);
        correctionPanel.setVisible(false);
    }

    private void updateEnabled() {
        boolean departmentSelected = departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty();
        boolean periodSelected = periodPicker.getValue() != null && !periodPicker.getValue().isEmpty();
        boolean correctionDateSelected = correctionDate.getText() != null && !correctionDate.getText().isEmpty();

        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(periodSelected);
        declarationTypeBox.setEnabled(departmentSelected);
        taxOrganCode.setEnabled(departmentSelected);
        taxOrganKpp.setEnabled(departmentSelected);
        // дата корректировки
        correctionPanel.setVisible(departmentSelected && correctionDateSelected);
    }


    @Override
    public void setAcceptableDeclarationTypes(List<DeclarationType> declarationTypes) {
        declarationTypesMap.clear();
        for (DeclarationType declarationType : declarationTypes) {
            declarationTypesMap.put(declarationType.getId(), declarationType);
        }
        declarationTypeBox.setValue(null);
        declarationTypeBox.setAcceptableValues(declarationTypesMap.keySet());
    }

    @Override
    public void setCorrectionDate(String correctionDate, TaxType taxType) {
        correctionPanel.setVisible(correctionDate != null);
        this.correctionDate.setText((correctionDate != null) ? ((taxType == TaxType.DEAL ? NOTIFICATION_CORRECTION : DECLARATION_CORRECTION) + correctionDate) : "");
    }

    @Override
    public void setAcceptableDepartments(List<Department> departments, Set<Integer> departmentsIds) {
        departmentPicker.setAvalibleValues(departments, departmentsIds);
    }

    @Override
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods) {
        periodPicker.setPeriods(reportPeriods);
    }

    @UiHandler("continueButton")
    public void onContinue(ClickEvent event) {
        getUiHandlers().onContinue();
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

    @UiHandler("periodPicker")
    public void onPeriodPickerChange(ValueChangeEvent<List<Integer>> event) {
        departmentPicker.setValue(null, true);
        if (getUiHandlers() != null) {
            getUiHandlers().onReportPeriodChange();
        }
        updateEnabled();
    }

    @UiHandler("departmentPicker")
    public void onDepartmentPickerChange(ValueChangeEvent<List<Integer>> event) {
        declarationTypeBox.setValue(null);
        taxOrganCode.setValue(null);
        taxOrganKpp.setValue(null);
        getUiHandlers().onDepartmentChange();
        updateEnabled();
    }

    @Override
    public void setSelectedDeclarationType(Integer id) {
        declarationTypeBox.setValue(id);
    }


    @Override
    public void setSelectedReportPeriod(List<Integer> periodIds) {
        periodPicker.setValue(periodIds);
    }


    @Override
    public void setSelectedDepartment(List<Integer> departmentIds) {
        departmentPicker.setValue(departmentIds);
    }

    @Override
    public void setSelectedTaxOrganCode(String code) {
        taxOrganCode.setValue(code);
    }

    @Override
    public void setSelectedTaxOrganKpp(String kpp) {
        taxOrganKpp.setValue(kpp);
    }

    @Override
    public Integer getSelectedDeclarationType() {
        return declarationTypeBox.getValue();
    }


    @Override
    public List<Integer> getSelectedReportPeriod() {
        return periodPicker.getValue();
    }


    @Override
    public List<Integer> getSelectedDepartment() {
        return departmentPicker.getValue();
    }

    @Override
    public void setTaxType(TaxType taxType) {
        if (!taxType.equals(TaxType.DEAL)) {
            modalWindowTitle.setText(DECLARATION_TITLE);
            declarationTypeLabel.setText(DECLARATION_TYPE_TITLE);
        } else {
            modalWindowTitle.setText(DECLARATION_TITLE_D);
            declarationTypeLabel.setText(DECLARATION_TYPE_TITLE_D);
        }
        if (!taxType.equals(TaxType.PROPERTY)) {
            codePanel.setVisible(false);
            kppPanel.setVisible(false);
        }

        declarationTypeBox.setVisible(true);
    }

    @Override
    public String getTaxOrganCode() {
        return taxOrganCode.getValue().trim();
    }

    @Override
    public String getTaxOrganKpp() {
        return taxOrganKpp.getValue().trim();
    }
}
