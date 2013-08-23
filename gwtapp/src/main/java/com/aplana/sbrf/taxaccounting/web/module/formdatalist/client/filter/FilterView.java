package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class FilterView extends ViewWithUiHandlers<FilterUIHandlers> implements FilterPresenter.MyView,
		Editor<FormDataFilter>, ReportPeriodSelectHandler {

    interface MyBinder extends UiBinder<Widget, FilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, FilterView>{
    }

    private final MyDriver driver;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> formTypeId;

    @UiField(provided = true)
	ValueListBox<FormDataKind> formDataKind;

	@UiField(provided = true)
	ValueListBox<WorkflowState> formState;

	@UiField
	Panel departmentSelectionTreePanel;

	@UiField
	VerticalPanel reportPeriodPanel;

	private final Map<TaxType, ReportPeriodPicker> taxTypeReportPeriodPickerMap = new HashMap<TaxType, ReportPeriodPicker>();
	private final Map<TaxType, DepartmentPickerPopupWidget> taxTypeDepartmentSelectionTree = new HashMap<TaxType, DepartmentPickerPopupWidget>();
	private ReportPeriodPicker currentReportPeriod;
	private DepartmentPickerPopupWidget currentDepartment;

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

		formTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return formTypesMap.get(object);
			}
		});

		initWidget(binder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);

	    for (TaxType taxType : TaxType.values()){
	    	ReportPeriodPicker periodPiker = new ReportPeriodPicker(this);
		    taxTypeReportPeriodPickerMap.put(taxType, periodPiker);
		    
		    DepartmentPickerPopupWidget depPiker = new DepartmentPickerPopupWidget("Выберите подразделение", true);
		    taxTypeDepartmentSelectionTree.put(taxType, depPiker);
	    }
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
		reportPeriodPanel.add(currentReportPeriod);
	}

	@Override
	public void updateDepartmentPicker(){
		if(currentDepartment != null){
			departmentSelectionTreePanel.remove(currentDepartment);
		}
		currentDepartment = taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType());
		departmentSelectionTreePanel.add(currentDepartment);
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
			for(Map.Entry<Integer, ReportPeriod> reportPeriod : taxTypeReportPeriodPickerMap
					.get(getUiHandlers().getCurrentTaxType()).getSelectedReportPeriods().entrySet()){
				selectedReportPeriodIds.add(reportPeriod.getKey());
			}
		}
		return selectedReportPeriodIds;
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
	public void setDepartmentsList(List<Department> list, Set<Integer> availableValues){
		if(getUiHandlers() != null){
			taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType()).setAvalibleValues(list, availableValues);
		}
	}

	@Override
	public void setSelectedDepartments(List<Integer> values){
		if(getUiHandlers() != null){
			taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType()).setValue(values, true);
		}
	}

	@Override
	public void setSelectedReportPeriods(List<ReportPeriod> reportPeriodList){
		if(getUiHandlers() != null){
			taxTypeReportPeriodPickerMap.get(getUiHandlers().getCurrentTaxType()).setSelectedReportPeriods(reportPeriodList);
		}
	}

	@Override
	public List<Integer> getSelectedDepartments(){
		if(getUiHandlers() != null){
			return taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType()).getValue();
		}
		return null;
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		if (taxPeriod!=null){
			getUiHandlers().onTaxPeriodSelected(taxPeriod);
		}
	}

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
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
