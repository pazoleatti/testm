package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
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
    PeriodPickerPopupWidget reportPeriodIds;

    @UiField
    DepartmentPicker departmentPicker;

	@UiField(provided = true)
	ListBoxWithTooltip<Integer> declarationTypeId;

	private Map<Integer, String> declarationTypeMap;

    @Inject
	@UiConstructor
    public DeclarationFilterView(final MyBinder binder, MyDriver driver) {

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
    }

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
	}
	@Override
	public void setDataFilter(DeclarationDataFilter formDataFilter) {
		driver.edit(formDataFilter);
		departmentPicker.setValue(formDataFilter.getDepartmentIds());
	}

	@Override
	public DeclarationDataFilter getFilterData() {
		DeclarationDataFilter dataFilter = driver.flush();
		dataFilter.setDepartmentIds(departmentPicker.getValue());
		return dataFilter;
	}

	@Override
	public void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments){
		departmentPicker.setAvalibleValues(list, availableDepartments);
	}

	@Override
	public void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap){
		this.declarationTypeMap = declarationTypeMap;
		/** .setValue(null) see
		 *  http://stackoverflow.com/questions/11176626/how-to-remove-null-value-from-valuelistbox-values **/
		declarationTypeId.setValue(null);
		declarationTypeId.setAcceptableValues(declarationTypeMap.keySet());
	}

	@UiHandler("apply")
	void onApplyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyFilter();
		}
	}

	@UiHandler("create")
	void onCreateButtonClicked(ClickEvent event){
		if (getUiHandlers() != null) {
			getUiHandlers().onCreateClicked();
		}
	}

}
