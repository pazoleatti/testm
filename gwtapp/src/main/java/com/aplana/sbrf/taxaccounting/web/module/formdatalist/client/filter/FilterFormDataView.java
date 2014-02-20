package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.FormDataElementName;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class FilterFormDataView extends ViewWithUiHandlers<FilterFormDataUIHandlers> implements FilterFormDataPresenter.MyView,
		Editor<FormDataFilter>{

    interface MyBinder extends UiBinder<Widget, FilterFormDataView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, FilterFormDataView>{
    }

    private final MyDriver driver;

    @UiField
    RefBookPickerWidget formTypeId;

    @UiField
    RefBookPickerWidget formDataKind;

	@UiField(provided = true)
	ValueListBox<WorkflowState> formState;

	@UiField(provided = true)
	ValueListBox<Boolean> returnState;

	@UiField
	PeriodPickerPopupWidget reportPeriodIds;

	@UiField
    @Path("departmentIds")
	DepartmentPicker departmentPicker;

	@Ignore
	@UiField
	Label departmentPickerLbl;

	@Ignore
	@UiField
	Label formDataKindLbl;

	@Ignore
	@UiField
	Label formTypeIdLbl;

	@Ignore
	@UiField
	Label formStateLbl;

	@Ignore
	@UiField
	Label returnStateLbl;

	@Ignore
	@UiField
	Label reportPeriodIdsLbl;

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

        // т.к. справочник не версионный, а дату выставлять обязательно
        formDataKind.setPeriodDates(new Date(), new Date());
        formTypeId.setPeriodDates(new Date(), new Date());
    }

    @Override
    public void setDataFilter(FormDataFilter formDataFilter) {
        driver.edit(formDataFilter);
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //departmentPicker.setValue(formDataFilter.getDepartmentIds());
    }


    @Override
    public FormDataFilter getDataFilter() {
    	FormDataFilter filter = driver.flush();
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
    	//filter.setDepartmentIds(departmentPicker.getValue());
        return filter;
    }

	@Override
	public void setFormStateList(List<WorkflowState> list){
		formState.setValue(null);
		formState.setAcceptableValues(list);
	}

	@Override
	public void setReturnStateList(List<Boolean> list) {
		returnState.setValue(null);
		returnState.setAcceptableValues(list);
	}


	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		reportPeriodIds.setPeriods(reportPeriods);
	}

	@Override
	public void setElementNames(Map<FormDataElementName, String> names) {
        String app = ":";
		for (Map.Entry<FormDataElementName, String> name : names.entrySet()) {
			if (name.getValue() == null) {
				continue;
			}
			switch (name.getKey()) {
				case DEPARTMENT:
					departmentPickerLbl.setText(name.getValue() + app);
					break;
				case FORM_KIND:
					formDataKindLbl.setText(name.getValue() + app);
					break;
				case FORM_TYPE:
					formTypeIdLbl.setText(name.getValue() + app);
					break;
				case REPORT_PERIOD:
					reportPeriodIdsLbl.setText(name.getValue() + app);
					break;
				case RETURN:
					returnStateLbl.setText(name.getValue() + app);
					break;
				case STATUS:
					formStateLbl.setText(name.getValue() + app);
					break;
				default:
					break;
			}
		}
	}

    @Override
    public void setFilter(String filter) {
        formTypeId.setFilter(filter);
    }

	@Override
	public void setDepartments(List<Department> list, Set<Integer> availableValues){
		departmentPicker.setAvalibleValues(list, availableValues);
	}

	@UiHandler("apply")
	void onAppyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyClicked();
		}
	}

    @Override
    public void setKindFilter(List<FormDataKind> dataKinds) {
        List<Integer> list = new ArrayList<Integer>(dataKinds.size());

        for (FormDataKind kind : dataKinds) {
            list.add(kind.getId());
        }
        formDataKind.setFilter(StringUtils.join(list.toArray(), ','));
    }
}
