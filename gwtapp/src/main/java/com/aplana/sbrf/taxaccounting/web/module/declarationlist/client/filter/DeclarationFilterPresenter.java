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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeclarationFilterPresenter extends PresenterWidget<DeclarationFilterPresenter.MyView>
		implements DeclarationFilterUIHandlers {

	public interface MyView extends View, HasUiHandlers<DeclarationFilterUIHandlers> {

		void setDepartmentsList(List<Department> list);

		List<Integer> getSelectedReportPeriods();

		void updateReportPeriodPicker();

		Map<String, Integer> getSelectedDepartments();

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
		return declarationFilter;
	}

	public void initFilter(final TaxType taxType) {
        this.taxType = taxType;
		getView().updateReportPeriodPicker();

		GetDeclarationFilterData action = new GetDeclarationFilterData();
        action.setTaxType(taxType);
        dispatchAsync.execute(action,
                new AbstractCallback<GetDeclarationFilterDataResult>() {
                    @Override
                    public void onReqSuccess(GetDeclarationFilterDataResult result) {
	                    getView().setDepartmentsList(result.getDepartments());
	                    getView().setTaxPeriods(result.getTaxPeriods());
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

}
