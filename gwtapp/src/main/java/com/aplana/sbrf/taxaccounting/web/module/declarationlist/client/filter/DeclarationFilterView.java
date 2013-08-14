package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodSelectHandler;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class DeclarationFilterView extends ViewWithUiHandlers<DeclarationFilterUIHandlers> implements DeclarationFilterPresenter.MyView,
        ReportPeriodSelectHandler {

	interface MyBinder extends UiBinder<Widget, DeclarationFilterView> {
    }

	@UiField
	Panel reportPeriodPanel;

	@UiField
	Panel departmentSelectionTreePanel;

	@UiField(provided = true)
	ListBoxWithTooltip<Integer> declarationType;

	private final Map<TaxType, ReportPeriodPicker> taxTypeReportPeriodPickerMap = new HashMap<TaxType, ReportPeriodPicker>();
	private final Map<TaxType, DepartmentPickerPopupWidget> taxTypeDepartmentSelectionTree = new HashMap<TaxType, DepartmentPickerPopupWidget>();
	private ReportPeriodPicker currentReportPeriod;
	private DepartmentPickerPopupWidget currentDepartment;
	private Map<Integer, String> declarationTypeMap;

    @Inject
	@UiConstructor
    public DeclarationFilterView(final MyBinder binder) {
	    for (TaxType taxType : TaxType.values()){
	    	final ReportPeriodPicker periodPiker = new ReportPeriodPicker(this);
	    	periodPiker.setEnabled(false);
		    taxTypeReportPeriodPickerMap.put(taxType, periodPiker);
		    // Убрал мультивыбор подразделения (http://jira.aplana.com/browse/SBRFACCTAX-3401)
		    DepartmentPickerPopupWidget depPiker = new DepartmentPickerPopupWidget("Выберите подразделение", false);
		    depPiker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
				@Override
				public void onValueChange(ValueChangeEvent<List<Integer>> event) {
					if (event.getValue().isEmpty()){
						periodPiker.clearReportPeriods();
						periodPiker.setEnabled(false);
					} else {
						periodPiker.clearReportPeriods();
						periodPiker.setEnabled(true);
					}
					
				}
			});
		    taxTypeDepartmentSelectionTree.put(taxType, depPiker);
	    }

	    declarationType = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
		    @Override
		    public String render(Integer object) {
			    if (object == null) {
				    return "";
			    }
			    return declarationTypeMap.get(object);
		    }
	    });

	    initWidget(binder.createAndBindUi(this));
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
	public void setSelectedReportPeriods(List<ReportPeriod> reportPeriodList){
		if(getUiHandlers() != null){
			taxTypeReportPeriodPickerMap.get(getUiHandlers().getCurrentTaxType()).setSelectedReportPeriods(reportPeriodList);
		}
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
	public void setDataFilter(DeclarationDataFilter declarationFilter, TaxType taxType) {
		declarationType.setValue(declarationFilter.getDeclarationTypeId());
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
		DepartmentPicker depPiker = taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType());
		if (taxPeriod!=null && !depPiker.getValue().isEmpty()){
			getUiHandlers().onTaxPeriodSelected(taxPeriod, depPiker.getValue().iterator().next());
		}
	}

    @Override
    public void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods) {
    }

    @Override
	public void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments){
		if(getUiHandlers() != null){
			taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType()).setAvalibleValues(list, availableDepartments);
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
	public Integer getSelectedDeclarationTypeId(){
		return declarationType.getValue();
	}

	@Override
	public void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap){
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
			for(Map.Entry<Integer, ReportPeriod> reportPeriod : taxTypeReportPeriodPickerMap
					.get(getUiHandlers().getCurrentTaxType()).getSelectedReportPeriods().entrySet()){
				selectedReportPeriodIds.add(reportPeriod.getKey());
			}
		}
		return selectedReportPeriodIds;
	}

	@Override
	public void setSelectedDepartments(List<Integer> values){
		if(getUiHandlers() != null){
			taxTypeDepartmentSelectionTree.get(getUiHandlers().getCurrentTaxType()).setValue(values);
		}
	}

}
