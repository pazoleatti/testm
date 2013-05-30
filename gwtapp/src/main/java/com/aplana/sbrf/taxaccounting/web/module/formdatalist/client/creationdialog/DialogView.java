package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.NewDepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodDataProvider;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DialogView extends PopupViewWithUiHandlers<DialogUiHandlers> implements DialogPresenter.MyView,
		ReportPeriodDataProvider{

	public interface Binder extends UiBinder<PopupPanel, DialogView> {
	}

	@UiField
	Panel reportPeriodPanel;

	@UiField
	Panel departmentPanel;

	@UiField(provided = true)
	ValueListBox<FormDataKind> formKind;

	@UiField(provided = true)
	ListBoxWithTooltip<FormType> formType;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;

	private final PopupPanel widget;
	private ReportPeriodPicker reportPeriodPicker;
	private NewDepartmentPicker departmentPicker;

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

		widget = uiBinder.createAndBindUi(this);
		widget.setAnimationEnabled(true);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void createDepartmentFilter(List<Department> list, Set<Integer> availableValues){
		departmentPicker = new NewDepartmentPicker("Выберите подразделение", false);
		departmentPicker.setTreeValues(list, availableValues);
	}

	@Override
	public void createReportPeriodFilter(List<TaxPeriod> taxPeriods){
		reportPeriodPicker = new ReportPeriodPicker(this, false);
		reportPeriodPicker.setTaxPeriods(taxPeriods);
	}

	@Override
	public void setupUI(){
		reportPeriodPanel.clear();
		departmentPanel.clear();

		reportPeriodPanel.add(new Label("Отчетный период"));
		reportPeriodPanel.add(reportPeriodPicker);
		departmentPanel.add(new Label("Подразделение"));
		departmentPanel.add(departmentPicker);
	}

	@UiHandler("continueButton")
	public void onSave(ClickEvent event){
		if(getUiHandlers() != null){
			getUiHandlers().onConfirm();
		}
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
		formDataFilter.setDepartmentId(new ArrayList<Integer>(departmentPicker.getSelectedItems().values()));
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
	public void setDepartmentValue(Map<String, Integer> value){
		departmentPicker.setSelectedItems(value);
	}

	@Override
	public void setReportPeriodValue(List<ReportPeriod> value) {
		reportPeriodPicker.setSelectedReportPeriods(value);
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		if(getUiHandlers() != null){
			getUiHandlers().onTaxPeriodSelected(taxPeriod);
		}
	}

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		reportPeriodPicker.setReportPeriods(reportPeriods);
	}

	@Override
	public void clearInput(){
		reportPeriodPicker.setSelectedReportPeriods(null);
		departmentPicker.setSelectedItems(null);
		formKind.setValue(null);
		formType.setValue(null);
	}

}
