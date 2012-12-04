package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter.SelectItem;
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

import java.util.ArrayList;
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
	ValueListBox<Integer> departmentId;

    @UiField(provided = true)
	ValueListBox<Integer> formTypeId;

    @UiField(provided = true)
	ValueListBox<FormDataKind> formDataKind;

	@UiField(provided = true)
	ValueListBox<WorkflowState> formState;

	private Map<Integer, String> formTypesMap;
	private Map<Integer, String> departmentMaps;
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

		departmentId = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return departmentMaps.get(object);
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
    public void setPeriodList(List<SelectItem<Integer>> list){
		List<Integer> reportPeriods = new ArrayList<Integer>();
		for(SelectItem<Integer> selectItem : list){
			if(selectItem.getId() == null){
				reportPeriods.add(null);
			} else {
				Integer reportPeriod = selectItem.getId();
				reportPeriods.add(reportPeriod);
			}
		}
		reportPeriodId.setAcceptableValues(reportPeriods);
    }

    @Override
    public void setDepartmentList(List<SelectItem<Integer>> list) {
		List<Integer> departments = new ArrayList<Integer>();
		for(SelectItem<Integer> selectItem : list){
			if(selectItem.getId() == null){
				departments.add(null);
			} else {
				Integer department = selectItem.getId();
				departments.add(department);
			}
		}
		departmentId.setAcceptableValues(departments);
    }


    @Override
    public void setFormtypeList(List<SelectItem<Integer>> list) {
		List<Integer> formTypes = new ArrayList<Integer>();
		for(SelectItem<Integer> selectItem : list){
			if(selectItem.getId() == null){
				formTypes.add(null);
			} else {
				Integer formType = selectItem.getId();
				formTypes.add(formType);
			}
		}
		formTypeId.setAcceptableValues(formTypes);
    }

    @Override
    public void setKindList(List<SelectItem<FormDataKind>> list) {
		List<FormDataKind> formDataKinds = new ArrayList<FormDataKind>();
		for(SelectItem<FormDataKind> selectItem : list){
			if(selectItem.getId() == null){
				formDataKinds.add(null);
			} else {
				FormDataKind formDataKind = selectItem.getId();
				formDataKinds.add(formDataKind);
			}
		}
		formDataKind.setAcceptableValues(formDataKinds);
    }

	@Override
	public void setFormStateList(List<SelectItem<WorkflowState>> list){
		List<WorkflowState> workflowStates = new ArrayList<WorkflowState>();
		for(SelectItem<WorkflowState> selectItem : list){
			if(selectItem.getId() == null){
				workflowStates.add(null);
			} else {
				WorkflowState workflowState = selectItem.getId();
				workflowStates.add(workflowState);
			}
		}
		formState.setAcceptableValues(workflowStates);
	}

	@Override
	public void setFormTypesMap(Map<Integer, String> formTypesMap){
		this.formTypesMap = formTypesMap;
		formTypeId.setAcceptableValues(formTypesMap.keySet());
	}

	@Override
	public void setReportPeriodMaps(Map<Integer, String> reportPeriodMaps) {
		this.reportPeriodMaps = reportPeriodMaps;
	}

	@Override
	public void setDepartmentMaps(Map<Integer, String> departmentMaps) {
		this.departmentMaps = departmentMaps;
	}

}
