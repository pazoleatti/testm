package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

public class CreateFormDataView extends PopupViewWithUiHandlers<CreateFormDataUiHandlers> implements CreateFormDataPresenter.MyView {

	public interface Binder extends UiBinder<PopupPanel, CreateFormDataView> {
	}

	@UiField
	DepartmentPicker departmentPicker;

	@UiField
	PeriodPicker reportPeriodPicker;

	@UiField(provided = true)
	ValueListBox<FormDataKind> formKind;

	@UiField(provided = true)
	ListBoxWithTooltip<Integer> formTypeId;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;	
	
	private Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();

	@Inject
	public CreateFormDataView(Binder uiBinder, EventBus eventBus) {
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

		formTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return formTypesMap.get(object);
			}
		});
		
		initWidget(uiBinder.createAndBindUi(this));		
	}

	@Override
	public void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues){
		departmentPicker.setAvalibleValues(list, availableValues);
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
	public void setAcceptableFormKindList(List<FormDataKind> list) {
		formKind.setValue(null);
		formKind.setAcceptableValues(list);
	}

	@Override
	public void setAcceptableFormTypeList(List<FormType> formTypes) {
		formTypesMap.clear();
		for (FormType formType : formTypes) {
			formTypesMap.put(formType.getId(), formType.getName());
		}
		
		formTypeId.setValue(null);
		formTypeId.setAcceptableValues(formTypesMap.keySet());
	}

	@Override
	public FormDataFilter getFilterData(){
		FormDataFilter formDataFilter = new FormDataFilter();
		formDataFilter.setFormDataKind(formKind.getValue());
		formDataFilter.setFormTypeId(formTypeId.getValue());
        formDataFilter.setDepartmentIds(departmentPicker.getValue());
		formDataFilter.setReportPeriodIds(reportPeriodPicker.getValue());
		return formDataFilter;
	}

	@Override
	public void setFormTypeValue(Integer value){
		formTypeId.setValue(value);
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
	public void setReportPeriodValue(List<Integer> value) {
		reportPeriodPicker.setValue(value);
	}

    @Override
	public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods) {
		reportPeriodPicker.setPeriods(reportPeriods);
	}

	@Override
	public void clearInput(){
		reportPeriodPicker.setValue(null);
		departmentPicker.setValue(null);
		formKind.setValue(null);
		formTypeId.setValue(null);
	}

}
