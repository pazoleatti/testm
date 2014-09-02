package com.aplana.sbrf.taxaccounting.web.module.periods.client.opencorrectdialog;

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

public class OpenCorrectDialogView extends PopupViewWithUiHandlers<OpenCorrectDialogUiHandlers>
		implements OpenCorrectDialogPresenter.MyView{

	public interface Binder extends UiBinder<PopupPanel, OpenCorrectDialogView> {
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

    @UiField
    Panel yearPnl;

    @UiField
    Panel termPnl;

    List<ReportPeriod> reportPeriods;

	@Inject
	public OpenCorrectDialogView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);
        initWidget(uiBinder.createAndBindUi(this));
    }

	@Override
	public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable) {
		departmentPicker.setAvalibleValues(departments, avalDepartments);
		departmentPicker.setEnabled(enable);
        setSelectedDepartment(selectedDepartments.get(0).getDepartmentId());
	}

	@Override
    public void setPeriodsList(List<ReportPeriod> reportPeriods, Integer reportPeriodId) {
        this.reportPeriods = reportPeriods;
        periodList.setPeriods(reportPeriods);
        periodList.setValue(Arrays.asList(reportPeriodId), true);
	}

    @Override
    public void setTaxType(TaxType taxType) {

    }

    @Override
    public void setSelectedDepartment(Integer departmentId){
        List<Integer> depId = new ArrayList<Integer>();
        depId.add(departmentId);
        departmentPicker.setValue(depId);
    }

    @Override
    public void resetForm() {
        periodList.setValue(null);
    }

    @UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
        Dialog.confirmMessage("Отмена операции открытия периода", "Отменить операцию открытия периода?", new DialogHandler() {
            @Override
            public void yes() {
                hide();
                super.yes();
            }
        });
	}
    @UiHandler("continueButton")
    public void onContinue(ClickEvent event) {
        getUiHandlers().onContinue();
    }

	@Override
	public void setCanChangeDepartment(boolean canChange) {
		departmentPicker.setEnabled(canChange);
	}

    @Override
    public List<Integer> getSelectedDepartments() {
        return departmentPicker.getValue();
    }

    @Override
    public Date getTerm() {
        return term.getValue();
    }

    @Override
    public ReportPeriod getSelectedPeriod() {
        if (periodList.getValue().size() == 1) {
            for(ReportPeriod reportPeriod : reportPeriods) {
                if (reportPeriod.getId().equals(periodList.getValue().get(0)))
                    return reportPeriod;
            }
        }
        return null;
    }

    @Override
    public boolean canChangeDepartment() {
        return  departmentPicker.isEnabled();
    }
}
