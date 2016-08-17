package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
    public static final String DECLARATION_CORRECTION = "Декларация будет создана в корректирующем периоде, дата сдачи корректировки: ";
    public static final String NOTIFICATION_CORRECTION = "Уведомление будет создано в корректирующем периоде, дата сдачи корректировки: ";

    @UiField
    ModalWindow modalWindowTitle;
    @UiField
    PeriodPickerPopupWidget periodPicker;
    @UiField
    DepartmentPickerPopupWidget departmentPicker;
    @UiField
    Label declarationTypeLabel;
    @UiField
    RefBookPickerWidget declarationTypeId;
    @UiField
    RefBookPickerWidget taxOrganCode;
    @UiField
    RefBookPicker taxOrganKpp;
    @UiField
    Label taxOrganCodeLabel;
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

    @Inject
    public DeclarationCreationView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        initWidget(uiBinder.createAndBindUi(this));
        //init();
    }

    @Override
    public void init() {
        departmentPicker.setEnabled(false);
        declarationTypeId.setEnabled(false);
        if (getUiHandlers().getTaxType().equals(TaxType.DEAL)) {
            declarationTypeId.setTitle("Выбор вида уведомления");
        } else {
            declarationTypeId.setTitle("Выбор вида декларации");
        }
        taxOrganCode.setEnabled(false);
        taxOrganKpp.setEnabled(false);
        correctionPanel.setVisible(false);
        declarationTypeId.setPeriodDates(new Date(), new Date());
        declarationTypeId.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                updateEnabled();
            }
        });
        taxOrganCode.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                updateEnabled();
                if (taxOrganCode.getFilter() != null) {
                    taxOrganKpp.setFilter(taxOrganCode.getFilter() + " and TAX_ORGAN_CODE = '" + taxOrganCode.getDereferenceValue().trim() + "'");
                    if (event.getValue() != null && !event.getValue().isEmpty() && !event.getValue().get(0).equals(taxOrganKpp.getSingleValue())) {
                        taxOrganKpp.setSingleValue(null);
                    }
                }
            }
        });
    }

    @Override
    public void initRefBooks(Date version, String filter, TaxType taxType) {
        taxOrganCode.setMultiSelect(false);
        taxOrganKpp.setMultiSelect(false);
        if (taxType == TaxType.TRANSPORT) {
            taxOrganCode.setAttributeId(3102L);
            taxOrganKpp.setAttributeId(3103L);
        }
        if (taxType == TaxType.PROPERTY) {
            taxOrganCode.setAttributeId(951L);
            taxOrganKpp.setAttributeId(952L);
        }

        if (taxType == TaxType.INCOME) {
            taxOrganCode.setAttributeId(3304L);
            taxOrganKpp.setAttributeId(3305L);
        }

        if (taxType == TaxType.LAND) {
            taxOrganCode.setAttributeId(7102L);
            taxOrganKpp.setAttributeId(7103L);
        }

        if (filter != null && !filter.isEmpty()) {
            taxOrganKpp.setFilter(filter);
            taxOrganCode.setFilter(filter);
            taxOrganKpp.setPeriodDates(version, version);
            taxOrganCode.setPeriodDates(version, version);
        } else {
            taxOrganKpp.setFilter("0 = 1");
            taxOrganCode.setFilter("0 = 1");
        }

        if (taxType == TaxType.TRANSPORT || taxType == TaxType.LAND) {
            taxOrganCode.setTitle("Код налогового органа (кон.)");
            taxOrganCodeLabel.setText("Код налогового органа (кон.)");
        } else {
            taxOrganCode.setTitle("Код налогового органа");
            taxOrganCodeLabel.setText("Код налогового органа");
        }
        taxOrganCode.setSingleColumn("TAX_ORGAN_CODE");
        taxOrganKpp.setSingleColumn("KPP");
    }

    @Override
    public void updateEnabled() {
        boolean departmentSelected =  departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty();
        boolean taxOrganCodeSelected = departmentSelected && taxOrganCode.getValue() != null && !taxOrganCode.getValue().isEmpty();
        boolean periodSelected = periodPicker.getValue() != null && !periodPicker.getValue().isEmpty();
        boolean correctionDateSelected = correctionDate.getText() != null && !correctionDate.getText().isEmpty();
        boolean declarationTypeIdSelected = declarationTypeId.getValue() != null && !declarationTypeId.getValue().isEmpty();
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(periodSelected);
        declarationTypeId.setEnabled(departmentSelected);
        taxOrganCode.setEnabled(departmentSelected);
        taxOrganKpp.setEnabled(codePanel.isVisible() ? taxOrganCodeSelected : departmentSelected);
        // дата корректировки
        correctionPanel.setVisible(departmentSelected && correctionDateSelected);
    }


    @Override
    public void setAcceptableDeclarationTypes(List<DeclarationType> declarationTypes) {
        declarationTypeId.setValue(null);

        if ((declarationTypes == null) || declarationTypes.isEmpty()) {
            /**
             * TODO продумать как сделать правильней,
             * на текущий момент синтаксис IN (..) не реализован в парсере фильтра,
             * так же нет варианта остановить подрузку на самом фронтенде
             */
            declarationTypeId.setFilter("2 = 1");
            return;
        }
        StringBuilder str = new StringBuilder();
        for (DeclarationType dt : declarationTypes) {
            str.append(RefBook.RECORD_ID_ALIAS + "=" + dt.getId() + " or ");
        }
        str.delete(str.length() - 3, str.length() - 1);
        declarationTypeId.setFilter(str.toString());
    }

    @Override
    public void setCorrectionDate(String correctionDate, TaxType taxType) {
        correctionPanel.setVisible(correctionDate != null);
        this.correctionDate.setText((correctionDate != null) ? ((taxType == TaxType.DEAL ? NOTIFICATION_CORRECTION : DECLARATION_CORRECTION) + correctionDate) : "");
    }

    @Override
    public void setAcceptableDepartments(List<Department> departments, Set<Integer> departmentsIds, Integer departmentsId) {
        departmentPicker.setAvalibleValues(departments, departmentsIds);
        if (departmentsId != null)
            departmentPicker.setValue(Arrays.asList(departmentsId));
    }

    @Override
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods, ReportPeriod reportPeriod) {
        periodPicker.setPeriods(reportPeriods);
        if (reportPeriod != null)
            periodPicker.setValue(Arrays.asList(reportPeriod.getId()));
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
        declarationTypeId.setValue(null);
        taxOrganCode.setValue(null);
        taxOrganKpp.setValue(null);
        if (getSelectedDepartment().isEmpty()) {
            declarationTypeId.setEnabled(false);
            taxOrganCode.setEnabled(false);
            taxOrganKpp.setEnabled(false);
        }
        getUiHandlers().onDepartmentChange();
    }

    @Override
    public void setSelectedDeclarationType(Integer id) {
        if (id == null) {
            declarationTypeId.setValue(null);
        } else {
            declarationTypeId.setValue(Arrays.asList(id.longValue()));
        }
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
        taxOrganCode.setDereferenceValue(code);
    }

    @Override
    public void setSelectedTaxOrganKpp(String kpp) {
        taxOrganKpp.setDereferenceValue(kpp);
    }

    @Override
    public Integer getSelectedDeclarationType() {
        List<Long> values = declarationTypeId.getValue();
        if (values != null && !values.isEmpty())
            return values.get(0).intValue();
        return null;
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
        periodPicker.setType(taxType.name());
        if (!taxType.equals(TaxType.DEAL)) {
            modalWindowTitle.setText(DECLARATION_TITLE);
            declarationTypeLabel.setText(DECLARATION_TYPE_TITLE);
        } else {
            modalWindowTitle.setText(DECLARATION_TITLE_D);
            declarationTypeLabel.setText(DECLARATION_TYPE_TITLE_D);
        }

        boolean isCodeKppVisible = taxType.equals(TaxType.PROPERTY) || taxType.equals(TaxType.TRANSPORT) || taxType.equals(TaxType.INCOME) || taxType.equals(TaxType.LAND);
        boolean isCodeVisible = taxType.equals(TaxType.PROPERTY) || taxType.equals(TaxType.TRANSPORT) || taxType.equals(TaxType.LAND);
        codePanel.setVisible(isCodeVisible);
        kppPanel.setVisible(isCodeKppVisible);

        declarationTypeId.setVisible(true);
    }

    @Override
    public String getTaxOrganCode() {
        return taxOrganCode.getDereferenceValue().trim();
    }

    @Override
    public String getTaxOrganKpp() {
        return taxOrganKpp.getDereferenceValue().trim();
    }

    @Override
    public Integer getDefaultReportPeriodId() {
        return periodPicker.getDefaultReportPeriod();
    }
}