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

    public static final String DECLARATION_TITLE = "Создание налоговой формы";
    public static final String DECLARATION_TITLE_D = "Создание уведомления";
    public static final String DECLARATION_TYPE_TITLE = "Вид налоговой формы:";
    public static final String DECLARATION_TYPE_TITLE_D = "Вид:";
    public static final String DECLARATION_CORRECTION = "Налоговая форма будет создана в корректирующем периоде, дата сдачи корректировки: ";
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
        correctionPanel.setVisible(false);
        declarationTypeId.setPeriodDates(new Date(), new Date());
        declarationTypeId.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                updateEnabled();
            }
        });
    }

    @Override
    public void initRefBooks(Date version, String filter, TaxType taxType) {
    }

    @Override
    public void updateEnabled() {
        boolean departmentSelected =  departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty();
        boolean periodSelected = periodPicker.getValue() != null && !periodPicker.getValue().isEmpty();
        boolean correctionDateSelected = correctionDate.getText() != null && !correctionDate.getText().isEmpty();
        boolean declarationTypeIdSelected = declarationTypeId.getValue() != null && !declarationTypeId.getValue().isEmpty();
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(periodSelected);
        declarationTypeId.setEnabled(departmentSelected);
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
        if (getSelectedDepartment().isEmpty()) {
            declarationTypeId.setEnabled(false);
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
    }

    @Override
    public void setSelectedTaxOrganKpp(String kpp) {
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

        boolean isCodeKppVisible = taxType.equals(TaxType.NDFL) || taxType.equals(TaxType.PFR);
        boolean isCodeVisible = taxType.equals(TaxType.NDFL) || taxType.equals(TaxType.PFR);

        declarationTypeId.setVisible(true);
    }

    @Override
    public Integer getDefaultReportPeriodId() {
        return periodPicker.getDefaultReportPeriod();
    }
}