package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.incrementbutton.IncrementButton;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;


public class OpenDialogView extends PopupViewWithUiHandlers<OpenDialogUiHandlers>
		implements OpenDialogPresenter.MyView, ReportPeriodSelectHandler {

	public interface Binder extends UiBinder<PopupPanel, OpenDialogView> {
	}

	private static final String DEPARTMENT_PICKER_HEADER = "Выбор подразделения";

	private final PopupPanel widget;
	private ReportPeriodPicker periodPicker;
    //private NewDepartmentPicker departmentPicker;

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

	@Inject
	public OpenDialogView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

		widget = uiBinder.createAndBindUi(this);
		widget.setAnimationEnabled(true);
	}

	@Override
	public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
		periodPicker = new ReportPeriodPicker(this, false);
		periodPicker.setTaxPeriods(taxPeriods);
	}

	@Override
	public void setDepartments(List<Department> departments, List<Integer> selectedDepartments) {
		departmentPicker.setAvalibleValues(departments, new HashSet<Integer>());
		departmentPicker.setValue(selectedDepartments);
	}

	@Override
	public void setCurrentReportPeriod(ReportPeriod reportPeriod) {
		periodPicker.setSelectedReportPeriods(Arrays.asList(reportPeriod));
	}

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		periodPicker.setReportPeriods(reportPeriods);
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
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		getUiHandlers().onTaxPeriodSelected(taxPeriod);
	}

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
    }

    @UiHandler("continueButton")
	public void onContinue(ClickEvent event) {
		OpenFilterData openFilterData = new OpenFilterData();
		openFilterData.setYear(yearBox.getValue());
		openFilterData.setBalancePeriod(balancePeriod.getValue());
		openFilterData.setDepartmentId(departmentPicker.getValue().iterator().next());
	    openFilterData.setDictionaryTaxPeriodId(period.getValue());
		openFilterData.setEndDate(term.getValue());

		getUiHandlers().onContinue(openFilterData);
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		hide();
	}
}
