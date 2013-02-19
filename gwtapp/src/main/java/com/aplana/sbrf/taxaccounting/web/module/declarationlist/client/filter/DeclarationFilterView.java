package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodDataProvider;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.treepicker.TreePicker;
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

public class DeclarationFilterView extends ViewWithUiHandlers<DeclarationFilterUIHandlers> implements DeclarationFilterPresenter.MyView,
		ReportPeriodDataProvider{

	interface MyBinder extends UiBinder<Widget, DeclarationFilterView> {
    }

    private final Widget widget;

	@UiField
	VerticalPanel reportPeriodPanel;

	@UiField
	TreePicker departmentSelectionTree;

	@UiField(provided = true)
	ValueListBox<Integer> declarationType;

	private final Map<TaxType, ReportPeriodPicker> taxTypeReportPeriodPickerMap = new HashMap<TaxType, ReportPeriodPicker>();
	private ReportPeriodPicker currentReportPeriod;
	private Map<Integer, String> declarationTypeMap;

    @Inject
	@UiConstructor
    public DeclarationFilterView(final MyBinder binder) {
	    for (TaxType taxType : TaxType.values()){
		    taxTypeReportPeriodPickerMap.put(taxType, new ReportPeriodPicker(this));
	    }

	    declarationType = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
		    @Override
		    public String render(Integer object) {
			    if (object == null) {
				    return "";
			    }
			    return declarationTypeMap.get(object);
		    }
	    });

	    widget = binder.createAndBindUi(this);
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
    public Widget asWidget() {
        return widget;
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
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		if(getUiHandlers() != null){
			getUiHandlers().onTaxPeriodSelected(taxPeriod);
		}
	}

	@Override
	public void setDepartmentsList(List<Department> list){
		departmentSelectionTree.setTreeValues(list);
	}

	@Override
	public Map<String, Integer> getSelectedDepartments(){
		return departmentSelectionTree.getSelectedItems();
	}

	@Override
	public Integer getSelectedDeclarationTypeId(){
		return declarationType.getValue();
	}

	@Override
	public void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap){
		declarationTypeMap.put(null, "");
		this.declarationTypeMap = declarationTypeMap;
		/** .setValue(null) see
		 *  http://stackoverflow.com/questions/11176626/how-to-remove-null-value-from-valuelistbox-values **/
		declarationType.setValue(null);
		declarationType.setAcceptableValues(declarationTypeMap.keySet());
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

}
