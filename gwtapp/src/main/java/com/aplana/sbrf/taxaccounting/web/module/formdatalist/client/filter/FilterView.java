package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.*;
import com.aplana.sbrf.taxaccounting.web.widget.treepicker.*;
import com.google.gwt.editor.client.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.text.shared.*;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.inject.*;
import com.gwtplatform.mvp.client.*;

import java.util.*;

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
	VerticalPanel departmentSelectionTreePanel;

	@UiField
	VerticalPanel reportPeriodPanel;

	private final Map<TaxType, ReportPeriodPicker> taxTypeReportPeriodPickerMap = new HashMap<TaxType, ReportPeriodPicker>();
	private final Map<TaxType, TreePicker> taxTypeDepartmentSelectionTree = new HashMap<TaxType, TreePicker>();
	private ReportPeriodPicker currentReportPeriod;
	private TreePicker currentDepartment;

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
		    taxTypeDepartmentSelectionTree.put(taxType, new TreePicker("Выберите подразделение"));
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
	public void setDepartmentsList(List<Department> list, Set<Integer> availableValues){
		if(getUiHandlers() != null){
			taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType()).setTreeValues(list, availableValues);
		}
	}

	@Override
	public void setSelectedDepartments(Map<String, Integer> values){
		if(getUiHandlers() != null){
			taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType()).setSelectedItems(values);
		}
	}

	@Override
	public void setSelectedReportPeriods(List<ReportPeriod> reportPeriodList){
		if(getUiHandlers() != null){
			taxTypeReportPeriodPickerMap.get(getUiHandlers().getCurrentTaxType()).setSelectedReportPeriods(reportPeriodList);
		}
	}

	@Override
	public Map<String, Integer> getSelectedDepartments(){
		if(getUiHandlers() != null){
			return taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType()).getSelectedItems();
		}
		return null;
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		if(getUiHandlers() != null){
			getUiHandlers().onTaxPeriodSelected(taxPeriod);
		}
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
