package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FilterFormDataView extends ViewWithUiHandlers<FilterFormDataUIHandlers> implements FilterFormDataPresenter.MyView,
		Editor<FormDataFilter>{

    interface MyBinder extends UiBinder<Widget, FilterFormDataView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, FilterFormDataView>{
    }

    private final MyDriver driver;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> formTypeId;

    @UiField(provided = true)
	ValueListBox<FormDataKind> formDataKind;

	@UiField(provided = true)
	ValueListBox<WorkflowState> formState;

	@UiField(provided = true)
	ValueListBox<Boolean> returnState;

	@UiField
	PeriodPickerPopupWidget reportPeriodIds;

	@UiField
	DepartmentPicker departmentPicker;

	private Map<Integer, String> formTypesMap;

    @Inject
    public FilterFormDataView(final MyBinder binder, final MyDriver driver) {
    	super();
    	
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

		formTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return formTypesMap.get(object);
			}
		});

	    returnState = new ListBoxWithTooltip<Boolean>(new AbstractRenderer<Boolean>() {
		    @Override
		    public String render(Boolean object) {
			    if (object == Boolean.TRUE) {
				    return "Возвращена";
			    } else if (object == Boolean.FALSE) {
				    return "Не возвращена";
			    } else {
				    return "";
			    }
		    }
	    });

		initWidget(binder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);
    }

    @Override
    public void setDataFilter(FormDataFilter formDataFilter) {
        driver.edit(formDataFilter);
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        departmentPicker.setValue(formDataFilter.getDepartmentIds());
    }


    @Override
    public FormDataFilter getDataFilter() {
    	FormDataFilter filter = driver.flush();
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
    	filter.setDepartmentIds(departmentPicker.getValue());
        return filter;
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
	public void setReturnStateList(List<Boolean> list) {
		returnState.setAcceptableValues(list);
	}


	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		reportPeriodIds.setPeriods(reportPeriods);
	}

	@Override
	public void setFormTypesMap(Map<Integer, String> formTypesMap){
		this.formTypesMap = formTypesMap;
		/** .setValue(null) see
		 *  http://stackoverflow.com/questions/11176626/how-to-remove-null-value-from-valuelistbox-values **/
		formTypeId.setValue(null);
		formTypeId.setAcceptableValues(formTypesMap.keySet());
	}

	@Override
	public void setDepartments(List<Department> list, Set<Integer> availableValues){
		departmentPicker.setAvalibleValues(list, availableValues);
	}

    @UiHandler("create")
	void onCreateButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCreateClicked();
		}
	}

	@UiHandler("apply")
	void onAppyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyClicked();
		}
	}
}
