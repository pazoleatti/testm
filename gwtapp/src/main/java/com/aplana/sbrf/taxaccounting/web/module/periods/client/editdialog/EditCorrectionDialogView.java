package com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class EditCorrectionDialogView extends PopupViewWithUiHandlers<EditCorrectionDialogUiHandlers>
        implements EditCorrectionDialogPresenter.MyView{

    public interface Binder extends UiBinder<PopupPanel, EditCorrectionDialogView> {
    }

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    @UiField
    PeriodPickerPopupWidget periodList;

    @UiField
    DateMaskBoxPicker term;

    List<ReportPeriod> reportPeriods;

    @Inject
    public EditCorrectionDialogView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable) {
        departmentPicker.setAvalibleValues(departments, avalDepartments);
        departmentPicker.setEnabled(enable);
    }


    @Override
    public void setTaxType(TaxType taxType) {
//        period.setFilter(taxType.getCode() + "=1");
    }

    @Override
    public void setSelectedDepartment(Integer departmentId){
        List<Integer> depId = new ArrayList<Integer>();
        depId.add(departmentId);
        departmentPicker.setValue(depId);
    }

    @UiHandler("continueButton")
    public void onContinue(ClickEvent event) {
        EditDialogData data = new EditDialogData();
        ReportPeriod reportPeriod = getSelectedPeriod();
        data.setReportPeriodId(reportPeriod == null ? null : reportPeriod.getId());
        data.setDepartmentId(departmentPicker.getValue().isEmpty() ? null : departmentPicker.getValue().get(0));
        data.setCorrectionDate(term.getValue());
        data.setPeriodYear(reportPeriod == null ? 0 : reportPeriod.getStartDate().getYear()+1900);
        getUiHandlers().onContinue(data);
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event){
        Dialog.confirmMessage("Отмена операции редактирования периода", "Отменить операцию редактирования периода?", new DialogHandler() {
            @Override
            public void yes() {
                hide();
                super.yes();    //To change body of overridden methods use File | Settings | File Templates.
            }
        });
    }

    @Override
    public void setCanChangeDepartment(boolean canChange) {
        departmentPicker.setEnabled(canChange);
    }

    @Override
    public void setPeriods(List<ReportPeriod> reportPeriods, Integer reportPeriodId) {
        this.reportPeriods = reportPeriods;
        periodList.setPeriods(reportPeriods);
        periodList.setValue(Arrays.asList(reportPeriodId), true);
    }

    private ReportPeriod getSelectedPeriod() {
        if (periodList.getValue().size() == 1) {
            for(ReportPeriod reportPeriod : reportPeriods) {
                if (reportPeriod.getId().equals(periodList.getValue().get(0)))
                    return reportPeriod;
            }
        }
        return null;
    }

    @Override
    public void setCorrectionDate(Date date) {
        term.setValue(date);
    }
}
