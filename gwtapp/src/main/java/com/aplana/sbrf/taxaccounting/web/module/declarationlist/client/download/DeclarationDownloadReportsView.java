package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.download;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class DeclarationDownloadReportsView extends PopupViewWithUiHandlers<DeclarationDownloadReportsUiHandlers>
        implements DeclarationDownloadReportsPresenter.MyView {

    public interface Binder extends UiBinder<PopupPanel, DeclarationDownloadReportsView> {
    }

    public static final String DECLARATION_TITLE_R = "Выгрузка отчетности";
    public static final String DECLARATION_TYPE_TITLE_R = "Вид отчетности:";
    public static final String NOTIFICATION_CORRECTION = "Отчетности будут выгружена в корректирующем периоде, дата сдачи корректировки: ";
    private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");

    @UiField
    ModalWindow modalWindowTitle;
    @UiField
    PeriodPickerPopupWidget periodPicker;
    @UiField(provided = true)
    ValueListBox<DepartmentReportPeriod> correction;
    @UiField
    DepartmentPickerPopupWidget departmentPicker;
    @UiField
    Label declarationTypeLabel;
    @UiField
    RefBookPickerWidget declarationTypeId;
    @UiField
    HorizontalPanel correctionPanel;
    @UiField
    Button continueButton;
    @UiField
    Button cancelButton;

    private boolean isCorrection;

    @Inject
    public DeclarationDownloadReportsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        correction = new ListBoxWithTooltip<DepartmentReportPeriod>(new AbstractRenderer<DepartmentReportPeriod>() {
            @Override
            public String render(DepartmentReportPeriod object) {
                if (object == null || object.getCorrectionDate() == null) {
                    return "";
                } else {
                    return DATE_TIME_FORMAT.format(object.getCorrectionDate());
                }
            }
        });
        initWidget(uiBinder.createAndBindUi(this));
        //init();
    }

    @Override
    public void init() {
        departmentPicker.setEnabled(false);
        declarationTypeId.setEnabled(false);
        declarationTypeId.setTitle("Выбор вида налоговой формы");
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
    public void updateEnabled() {
        boolean departmentSelected = departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty();
        boolean periodSelected = periodPicker.getValue() != null && !periodPicker.getValue().isEmpty();
        //boolean correctionDateSelected = correctionDate.getText() != null && !correctionDate.getText().isEmpty();
        //boolean declarationTypeIdSelected = declarationTypeId.getValue() != null && !declarationTypeId.getValue().isEmpty();
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(periodSelected);
        declarationTypeId.setEnabled(departmentSelected);
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
    public void setCorrectionDate(List<DepartmentReportPeriod> departmentReportPeriods) {
        if (departmentReportPeriods != null && departmentReportPeriods.size() > 1) {
            correctionPanel.setVisible(true);
            correction.setValue(departmentReportPeriods.get(departmentReportPeriods.size() - 1));
            correction.setAcceptableValues(departmentReportPeriods);
            isCorrection = true;
        } else {
            correctionPanel.setVisible(false);
            correction.setValue(null);
            isCorrection = false;
        }
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
        Dialog.confirmMessage("Отмена выгрузки", "Отменить выгрузку?", new DialogHandler() {
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
            setCorrectionDate(null);
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
    public boolean isCorrection() {
        return false;
    }

    @Override
    public Date getCorrectionDate() {
        if (correction.getValue() != null) {
            return correction.getValue().getCorrectionDate();
        } else {
            return null;
        }
    }

    @Override
    public void setTaxType(TaxType taxType) {
        periodPicker.setType(taxType.name());
        modalWindowTitle.setText(DECLARATION_TITLE_R);
        declarationTypeLabel.setText(DECLARATION_TYPE_TITLE_R);

        declarationTypeId.setVisible(true);
    }

    @Override
    public Integer getDefaultReportPeriodId() {
        return periodPicker.getDefaultReportPeriod();
    }
}