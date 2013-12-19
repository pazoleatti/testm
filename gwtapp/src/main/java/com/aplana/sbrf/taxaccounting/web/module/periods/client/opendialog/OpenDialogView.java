package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.incrementbutton.IncrementButton;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;


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
	IncrementButton yearBox;

	@UiField
	CheckBox balancePeriod;

	@UiField
	CustomDateBox term;

	@UiField
	RefBookPickerPopupWidget period;

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
	public void setDepartments(List<Department> departments, Set<Integer> avalDepartments, List<Integer> selectedDepartments, boolean enable) {
		departmentPicker.setAvalibleValues(departments, avalDepartments);
		departmentPicker.setValue(selectedDepartments);
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
	public void setBalance(boolean balance) {
		balancePeriod.setValue(balance);
	}

	@Override
	public void setSelectedDepartment(Department dep) {
		List<Integer> depId = new ArrayList<Integer>();
		depId.add(dep.getId());
		departmentPicker.setValue(depId);
	}

	@Override
	public boolean isYearEmpty() {
		return yearBox.isEmpty();
	}

    @UiHandler("continueButton")
	public void onContinue(ClickEvent event) {
		OpenFilterData openFilterData = new OpenFilterData();
		openFilterData.setYear(yearBox.isEmpty() ? null : yearBox.getValue());
		openFilterData.setBalancePeriod(balancePeriod.getValue());
		openFilterData.setDepartmentId(Long.valueOf(departmentPicker.getValue().iterator().next()));
	    openFilterData.setDictionaryTaxPeriodId(period.getValue());
        if (correctPeriod.getValue()) {
            openFilterData.setHasCorrectPeriod(true);
            openFilterData.setCorrectPeriod(term.getValue());
        } else {
            openFilterData.setHasCorrectPeriod(false);
            openFilterData.setEndDate(term.getValue());
        }

		getUiHandlers().onContinue(openFilterData);
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		hide();
	}

    @UiHandler("correctPeriod")
    public void onCorrectPeriodButton(ClickEvent event) {
        if (correctPeriod.getValue()) {
            yearPnl.setVisible(false);
            termPnl.setVisible(true);
            period.setTitle("Период корректировки");
            periodLbl.setText("Период корректировки");
        } else {
            yearPnl.setVisible(true);
            termPnl.setVisible(false);
            period.setTitle("Период");
            periodLbl.setText("Период");
        }
    }
}
