package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.web.widget.treepicker.TreePicker;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import java.util.List;
import java.util.Map;

public class FilterView extends ViewImpl implements FilterPresenter.MyView, Editor<FormDataFilter>{

    interface MyBinder extends UiBinder<Widget, FilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, FilterView>{
    }

    private final Widget widget;

    private final MyDriver driver;

    @UiField(provided = true)
	ValueListBox<Integer> reportPeriodId;

    @UiField(provided = true)
	ValueListBox<Integer> formTypeId;

    @UiField(provided = true)
	ValueListBox<FormDataKind> formDataKind;

	@UiField(provided = true)
	ValueListBox<WorkflowState> formState;

	@UiField
	TreePicker departmentSelectionTree;

	private Map<Integer, String> formTypesMap;
	private Map<Integer, String> reportPeriodMaps;

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

		reportPeriodId = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return reportPeriodMaps.get(object);
			}
		});

        widget = binder.createAndBindUi(this);
        this.driver = driver;
        this.driver.initialize(this);
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
    public void setKindList(List<FormDataKind> list) {
		formDataKind.setAcceptableValues(list);
    }

	@Override
	public void setFormStateList(List<WorkflowState> list){
		formState.setAcceptableValues(list);
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
	public void setReportPeriodMaps(Map<Integer, String> reportPeriodMaps) {
		reportPeriodMaps.put(null, "");
		this.reportPeriodMaps = reportPeriodMaps;
		/** .setValue(null) see
		 *  http://stackoverflow.com/questions/11176626/how-to-remove-null-value-from-valuelistbox-values **/
		reportPeriodId.setValue(null);
		reportPeriodId.setAcceptableValues(reportPeriodMaps.keySet());
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

}
