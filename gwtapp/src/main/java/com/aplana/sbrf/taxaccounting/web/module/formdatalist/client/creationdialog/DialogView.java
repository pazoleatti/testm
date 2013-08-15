package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

public class DialogView extends PopupViewWithUiHandlers<DialogUiHandlers> implements DialogPresenter.MyView,
        ReportPeriodSelectHandler {

	public interface Binder extends UiBinder<PopupPanel, DialogView> {
	}

	@UiField
	DepartmentPicker departmentPicker;

	@UiField(provided=true)
	ReportPeriodPicker reportPeriodPicker;

	@UiField(provided = true)
	ValueListBox<FormDataKind> formKind;

	@UiField(provided = true)
	ListBoxWithTooltip<FormType> formType;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;	


	@Inject
	@UiConstructor
	public DialogView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

		formKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
			@Override
			public String render(FormDataKind object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		formType = new ListBoxWithTooltip<FormType>(new AbstractRenderer<FormType>() {
			@Override
			public String render(FormType object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		reportPeriodPicker = new ReportPeriodPicker(this, false);
		reportPeriodPicker.setEnabled(false);

		
		initWidget(uiBinder.createAndBindUi(this));
		
		departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<Integer>> event) {
				reportPeriodPicker.clearReportPeriods();
				reportPeriodPicker.setEnabled(!event.getValue().isEmpty());
			}
		});
		
	}

	@Override
	public void setAvalibleDepartments(List<Department> list, Set<Integer> availableValues){
		departmentPicker.setAvalibleValues(list, availableValues);
	}

	@Override
	public void setAvalibleTaxPeriods(List<TaxPeriod> taxPeriods){
		reportPeriodPicker.setTaxPeriods(taxPeriods);
	}

	@UiHandler("continueButton")
	public void onSave(ClickEvent event){
		getUiHandlers().onConfirm();
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		clearInput();
		hide();
	}

	@Override
	public void setKindList(List<FormDataKind> list) {
		formKind.setAcceptableValues(list);
	}

	@Override
	public void setFormTypeList(List<FormType> list) {
		formType.setAcceptableValues(list);
	}

	@Override
	public FormDataFilter getFilterData(){
		FormDataFilter formDataFilter = new FormDataFilter();
		formDataFilter.setFormDataKind(formKind.getValue());
		formDataFilter.setFormTypeId(formType.getValue() != null ? formType.getValue().getId() : null);
		formDataFilter.setDepartmentId(new ArrayList<Integer>(departmentPicker.getValue()));
		formDataFilter.setReportPeriodIds(new ArrayList<Integer>(reportPeriodPicker.getSelectedReportPeriods().keySet()));
		return formDataFilter;
	}

	@Override
	public void setFormTypeValue(FormType value){
		formType.setValue(value);
	}

	@Override
	public void setFormKindValue(FormDataKind value){
		formKind.setValue(value);
	}

	@Override
	public void setDepartmentValue(List<Integer> value){
		departmentPicker.setValue(value, true);
	}

	@Override
	public void setReportPeriodValue(List<ReportPeriod> value) {
		reportPeriodPicker.setSelectedReportPeriods(value);
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		if(getUiHandlers() != null){
			getUiHandlers().onTaxPeriodSelected(taxPeriod, departmentPicker.getValue().iterator().next());
		}
	}

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
    }

    @Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		reportPeriodPicker.setReportPeriods(reportPeriods);
	}

	@Override
	public void clearInput(){
		reportPeriodPicker.setSelectedReportPeriods(null);
		departmentPicker.setValue(null);
		formKind.setValue(null);
		formType.setValue(null);
	}

}
