package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodDataProvider;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.treepicker.TreePicker;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterView extends ViewWithUiHandlers<FilterUIHandlers> implements FilterPresenter.MyView,
		Editor<FormDataFilter>, ReportPeriodDataProvider {

    interface MyBinder extends UiBinder<Widget, FilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, FilterView>{
    }

    private final Widget widget;

    private final MyDriver driver;

    @UiField(provided = true)
	ValueListBox<Integer> formTypeId;

    @UiField(provided = true)
	ValueListBox<FormDataKind> formDataKind;

	@UiField(provided = true)
	ValueListBox<WorkflowState> formState;

	@UiField
	TreePicker departmentSelectionTree;

	@UiField
	VerticalPanel reportPeriodPanel;

	private final Map<TaxType, ReportPeriodPicker> taxTypeReportPeriodPickerMap = new HashMap<TaxType, ReportPeriodPicker>();
	private ReportPeriodPicker currentReportPeriod;

	private Map<Integer, String> formTypesMap;

    @Inject
	@UiConstructor
    public FilterView(final MyBinder binder, final MyDriver driver) {
		formState = new ValueListBox<WorkflowState>(new AbstractRenderer<WorkflowState>() {
			@Override
			public String render(WorkflowState object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		formDataKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
			@Override
			public String render(FormDataKind object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		formTypeId = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return formTypesMap.get(object);
			}
		});

        widget = binder.createAndBindUi(this);
        this.driver = driver;
        this.driver.initialize(this);

	    for (TaxType taxType : TaxType.values()){
		    taxTypeReportPeriodPickerMap.put(taxType, new ReportPeriodPicker(this));
	    }
    }


    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public void setDataFilter(FormDataFilter formDataFilter) {
        driver.edit(formDataFilter);
    }


    @Override
    public FormDataFilter getDataFilter() {
        return driver.flush();
    }

	@Override
	public void updateReportPeriodPicker(){
		if(currentReportPeriod != null){
			reportPeriodPanel.remove(currentReportPeriod);
		}
		currentReportPeriod = taxTypeReportPeriodPickerMap.get(getUiHandlers().getCurrentTaxType());
		if(getUiHandlers() != null){
			reportPeriodPanel.add(currentReportPeriod);
		}
	}

    @Override
    public void setKindList(List<FormDataKind> list) {
		formDataKind.setAcceptableValues(list);
    }

	@Override
	public void setFormStateList(List<WorkflowState> list){
		formState.setAcceptableValues(list);
	}

	@Override
	public void setTaxPeriods(List<TaxPeriod> taxPeriods){
		if(getUiHandlers() != null){
			taxTypeReportPeriodPickerMap.get(getUiHandlers().getCurrentTaxType()).setTaxPeriods(taxPeriods);
		}
	}

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		if(getUiHandlers() != null){
			taxTypeReportPeriodPickerMap.get(getUiHandlers().getCurrentTaxType()).setReportPeriods(reportPeriods);
		}
	}

	@Override
	public List<Integer> getSelectedReportPeriods(){
		List<Integer> selectedReportPeriodIds = new ArrayList<Integer>();
		if(getUiHandlers() != null){
			for(Map.Entry<Integer, String> reportPeriod : taxTypeReportPeriodPickerMap
					.get(getUiHandlers().getCurrentTaxType()).getSelectedReportPeriods().entrySet()){
				selectedReportPeriodIds.add(reportPeriod.getKey());
			}
		}
		return selectedReportPeriodIds;
	}

	@Override
	public void setFormTypesMap(Map<Integer, String> formTypesMap){
		formTypesMap.put(null, "");
		this.formTypesMap = formTypesMap;
		/** .setValue(null) see
		 *  http://stackoverflow.com/questions/11176626/how-to-remove-null-value-from-valuelistbox-values **/
		formTypeId.setValue(null);
		formTypeId.setAcceptableValues(formTypesMap.keySet());
	}

	@Override
	public void setDepartmentsList(List<Department> list){
		departmentSelectionTree.setTreeValues(list);
	}

	@Override
	public void setSelectedDepartments(Map<String, Integer> values){
		departmentSelectionTree.setSelectedItems(values);
	}

	@Override
	public Map<String, Integer> getSelectedDepartments(){
		return departmentSelectionTree.getSelectedItems();
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		if(getUiHandlers() != null){
			getUiHandlers().onTaxPeriodSelected(taxPeriod);
		}
	}
}
