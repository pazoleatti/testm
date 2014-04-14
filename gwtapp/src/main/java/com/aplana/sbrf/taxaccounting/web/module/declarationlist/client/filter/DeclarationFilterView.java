package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.gwt.client.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeclarationFilterView extends ViewWithUiHandlers<DeclarationFilterUIHandlers>
        implements DeclarationFilterPresenter.MyView, Editor<DeclarationDataFilter> {

	interface MyBinder extends UiBinder<Widget, DeclarationFilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<DeclarationDataFilter, DeclarationFilterView> {}

    private MyDriver driver;

    @UiField
    @Ignore
    Label reportPeriodIdsLabel;

    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    @UiField
    @Path("departmentIds")
    DepartmentPicker departmentPicker;

    @UiField
    @Ignore
    Label declarationTypeLabel;

	@UiField(provided = true)
	ListBoxWithTooltip<Integer> declarationTypeId;

    @UiField(provided = true)
    ValueListBox<WorkflowState> formState;

	private Map<Integer, String> declarationTypeMap;

    @Inject
	@UiConstructor
    public DeclarationFilterView(final MyBinder binder, MyDriver driver) {
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

		declarationTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return declarationTypeMap.get(object);
			}
		});

	    initWidget(binder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);
       /* reportPeriodIds.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });*/
    }

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
	}

    @Override
    public void setFormStateList(List<WorkflowState> list){
        formState.setValue(null);
        formState.setAcceptableValues(list);
    }

    @Override
	public void setDataFilter(DeclarationDataFilter formDataFilter) {
		driver.edit(formDataFilter);
		//departmentPicker.setValue(formDataFilter.getDepartmentIds());
	}

	@Override
	public DeclarationDataFilter getFilterData() {
		DeclarationDataFilter dataFilter = driver.flush();
        //dataFilter.setDepartmentIds(departmentPicker.getValue());
		return dataFilter;
	}

	@Override
	public void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments){
		departmentPicker.setAvalibleValues(list, availableDepartments);
	}

	@Override
	public void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap){
		this.declarationTypeMap = declarationTypeMap;
		declarationTypeId.setValue(null);
		declarationTypeId.setAcceptableValues(declarationTypeMap.keySet());
	}

	@UiHandler("apply")
	void onApplyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyFilter();
		}
	}

    @Override
    public void updateFilter(TaxType taxType) {
        boolean isTaxTypeDeal = taxType.equals(TaxType.DEAL);
        declarationTypeId.setVisible(!isTaxTypeDeal);
        declarationTypeLabel.setVisible(!isTaxTypeDeal);
        reportPeriodIds.setVisible(!isTaxTypeDeal);
        reportPeriodIdsLabel.setVisible(!isTaxTypeDeal);
    }

    @Override
    public void clean() {
        formState.setValue(null);
        declarationTypeId.setValue(null);
        reportPeriodIds.setValue(null);
        departmentPicker.setValue(null);
        if (getUiHandlers() != null) {
            getUiHandlers().onApplyFilter();
        }
    }
}
