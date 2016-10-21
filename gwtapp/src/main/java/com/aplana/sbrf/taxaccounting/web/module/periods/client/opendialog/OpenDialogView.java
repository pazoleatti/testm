package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import com.aplana.gwt.client.Spinner;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class OpenDialogView extends PopupViewWithUiHandlers<OpenDialogUiHandlers>
		implements OpenDialogPresenter.MyView{

	public interface Binder extends UiBinder<PopupPanel, OpenDialogView> {
	}

	@UiField
	DepartmentPickerPopupWidget departmentPicker;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;

	@UiField
	Spinner yearBox;

	@UiField
	CheckBox balancePeriod;

	@UiField
    DateMaskBoxPicker term;

	@UiField
    RefBookPickerWidget period;

    @UiField
    CheckBox correctPeriod;

    @UiField
    Label periodLbl;

    @UiField
    Panel yearPnl;

    @UiField
    Panel termPnl;

	@Inject
	public OpenDialogView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<DepartmentPair> selectedDepartments, boolean enable) {
		departmentPicker.setAvalibleValues(departments, avalDepartments);
		departmentPicker.setEnabled(enable);
	}

	@Override
	public void setYear(int year) {
		yearBox.setValue(year);
	}

	@Override
	public void setTaxType(TaxType taxType) {
		period.setFilter(taxType.getCode() + "=1");
	}

    @Override
    public void setSelectedDepartment(Integer departmentId){
        List<Integer> depId = new ArrayList<Integer>();
        depId.add(departmentId);
        departmentPicker.setValue(depId);
    }

	@UiHandler("continueButton")
	public void onContinue(ClickEvent event) {
		OpenFilterData openFilterData = new OpenFilterData();
		openFilterData.setYear(yearBox.getValue());
		openFilterData.setBalancePeriod(balancePeriod.getValue());
		openFilterData.setDepartmentId(departmentPicker.getValue().isEmpty() ? null : departmentPicker.getValue().get(0));
	    openFilterData.setDictionaryTaxPeriodId(period.getSingleValue());
        if (correctPeriod.getValue()) {
            openFilterData.setCorrectPeriod(term.getValue());
        } else {
            openFilterData.setEndDate(term.getValue());
        }

		getUiHandlers().onContinue(openFilterData);
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
        Dialog.confirmMessage("Отмена операции открытия периода", "Отменить операцию открытия периода?", new DialogHandler() {
            @Override
            public void yes() {
                hide();
                super.yes();    //To change body of overridden methods use File | Settings | File Templates.
            }
        });
	}

    @UiHandler("correctPeriod")
    public void onCorrectPeriodButton(ClickEvent event) {
        onCorrectPeriodButton();
    }

    private void onCorrectPeriodButton(){
        if (correctPeriod.getValue()) {
                yearPnl.setVisible(false);
        termPnl.setVisible(true);
        period.setTitle("Период корректировки");
        periodLbl.setText("Период корректировки:");
        } else {
                yearPnl.setVisible(true);
        termPnl.setVisible(false);
        period.setTitle("Период");
        periodLbl.setText("Период:");
        }
    }

    @Override
    public void resetForm(){
        departmentPicker.setValue(null);
        period.setValue(null, true);
        period.setDereferenceValue(null);
	    Date current = new Date();
	    period.setPeriodDates(current, current);
        balancePeriod.setValue(false);
        correctPeriod.setValue(false, true);
        onCorrectPeriodButton();
        term.setValue(null);
    }

	@Override
	public void setCanChangeDepartment(boolean canChange) {
		departmentPicker.setEnabled(canChange);
	}
}
