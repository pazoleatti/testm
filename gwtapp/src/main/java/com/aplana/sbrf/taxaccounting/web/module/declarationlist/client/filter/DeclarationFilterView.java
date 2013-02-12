package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodDataProvider;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.treepicker.TreePicker;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
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

	private final ReportPeriodPicker reportPeriodPicker;

    @Inject
	@UiConstructor
    public DeclarationFilterView(final MyBinder binder) {
	    widget = binder.createAndBindUi(this);
	    reportPeriodPicker = new ReportPeriodPicker(this);
	    reportPeriodPanel.add(reportPeriodPicker);
    }


    @Override
    public Widget asWidget() {
        return widget;
    }

	@Override
	public void setTaxPeriods(List<TaxPeriod> taxPeriods){
		reportPeriodPicker.setTaxPeriods(taxPeriods);
	}

	@Override
	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		reportPeriodPicker.setReportPeriods(reportPeriods);
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
	public List<Integer> getSelectedReportPeriods(){
		List<Integer> selectedReportPeriodIds = new ArrayList<Integer>();
		for(Map.Entry<Integer, String> reportPeriod : reportPeriodPicker.getSelectedReportPeriods().entrySet()){
			selectedReportPeriodIds.add(reportPeriod.getKey());
		}
		return selectedReportPeriodIds;
	}

}
