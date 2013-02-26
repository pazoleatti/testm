package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterData;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterDataResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriods;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.*;

public class DeclarationFilterPresenter extends PresenterWidget<DeclarationFilterPresenter.MyView>
		implements DeclarationFilterUIHandlers {

	public interface MyView extends View, HasUiHandlers<DeclarationFilterUIHandlers> {

		void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments);

		List<Integer> getSelectedReportPeriods();

		void updateReportPeriodPicker();

		void updateDepartmentPicker();

		Map<String, Integer> getSelectedDepartments();

		Integer getSelectedDeclarationTypeId();

		void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap);

		void setTaxPeriods(List<TaxPeriod> taxPeriods);

		void setReportPeriods(List<ReportPeriod> reportPeriods);
	}

	private final DispatchAsync dispatchAsync;
	
	private TaxType taxType;

	@Inject
	public DeclarationFilterPresenter(EventBus eventBus, MyView view,
	                                  DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	public DeclarationFilter getFilterData() {
		DeclarationFilter declarationFilter = new DeclarationFilter();

		declarationFilter.setReportPeriodIds(new ArrayList<Integer>(getView().getSelectedReportPeriods()));
		declarationFilter.setDepartmentIds(new ArrayList<Integer>(getView().getSelectedDepartments().values()));
		declarationFilter.setTaxType(this.taxType);
		declarationFilter.setDeclarationTypeId(getView().getSelectedDeclarationTypeId());
		return declarationFilter;
	}

	public void initFilter(final TaxType taxType) {
        this.taxType = taxType;
		getView().updateReportPeriodPicker();
		getView().updateDepartmentPicker();

		GetDeclarationFilterData action = new GetDeclarationFilterData();
        action.setTaxType(taxType);
        dispatchAsync.execute(action,
                new AbstractCallback<GetDeclarationFilterDataResult>() {
                    @Override
                    public void onReqSuccess(GetDeclarationFilterDataResult result) {
	                    DeclarationFilterAvailableValues filterValues = result.getFilterValues();

		                getView().setDepartmentsList(result.getDepartments(), filterValues.getDepartmentIds());
	                    getView().setTaxPeriods(result.getTaxPeriods());
	                    getView().setDeclarationTypeMap(fillDeclarationTypesMap(result.getFilterValues()));
                        DeclarationFilterReadyEvent.fire(DeclarationFilterPresenter.this);
                    }
                });
	}

	@Override
	public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
		GetReportPeriods action = new GetReportPeriods();
		action.setTaxPeriod(taxPeriod);
		dispatchAsync.execute(action,
				new AbstractCallback<GetReportPeriodsResult>() {
					@Override
					public void onReqSuccess(GetReportPeriodsResult result) {
						getView().setReportPeriods(result.getReportPeriods());
					}
				});
	}

	@Override
	public TaxType getCurrentTaxType(){
		return this.taxType;
	}

	private Map<Integer, String> fillDeclarationTypesMap(DeclarationFilterAvailableValues source){
		Map<Integer, String> declarationTypeMap = new HashMap<Integer, String>();
		for(DeclarationType declarationType : source.getDeclarationTypes()){
			declarationTypeMap.put(declarationType.getId(), declarationType.getName());
		}
		return declarationTypeMap;
	}

}
