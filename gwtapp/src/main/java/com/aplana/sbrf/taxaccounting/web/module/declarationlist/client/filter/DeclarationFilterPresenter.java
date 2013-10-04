package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DetectUserRoleAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.DetectUserRoleResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterData;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.*;

public class DeclarationFilterPresenter extends PresenterWidget<DeclarationFilterPresenter.MyView>
		implements DeclarationFilterUIHandlers {

    @Override
    public void onApplyFilter() {
        DeclarationFilterApplyEvent.fire(this);
    }

    @Override
    public void onCreateClicked() {
        DeclarationFilterCreateEvent.fire(this);
    }

    public interface MyView extends View, HasUiHandlers<DeclarationFilterUIHandlers> {
		void setDataFilter(DeclarationDataFilter formDataFilter);

        DeclarationDataFilter getFilterData();

		void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments);

		void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap);

		void setReportPeriods(List<ReportPeriod> reportPeriods);

	}

	private final DispatchAsync dispatchAsync;

    @Inject
	public DeclarationFilterPresenter(EventBus eventBus, MyView view,
	                                  DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
		detectUserRoles();
	}

	public DeclarationDataFilter getFilterData() {
		return getView().getFilterData();
	}

	public void initFilter(final TaxType taxType, final DeclarationDataFilter dataFilter) {

		GetDeclarationFilterData action = new GetDeclarationFilterData();
        action.setTaxType(taxType);
        dispatchAsync.execute(action, CallbackUtils
				        .defaultCallback(new AbstractCallback<GetDeclarationFilterDataResult>() {
						@Override
						public void onSuccess(GetDeclarationFilterDataResult result) {
                            DeclarationDataFilterAvailableValues filterValues = result.getFilterValues();

							getView().setDepartmentsList(result.getDepartments(), filterValues.getDepartmentIds());
							getView().setReportPeriods(result.getPeriods());
							getView().setDeclarationTypeMap(fillDeclarationTypesMap(filterValues));

                            if (dataFilter != null){
                                getView().setDataFilter(dataFilter);
                            } else {
                                getView().setDataFilter(result.getDefaultDecFilterData());
                            }
							DeclarationFilterReadyEvent.fire(DeclarationFilterPresenter.this);
						}
					}, this));
	}

	private Map<Integer, String> fillDeclarationTypesMap(DeclarationDataFilterAvailableValues source){
		Map<Integer, String> declarationTypeMap = new LinkedHashMap<Integer, String>();
		declarationTypeMap.put(null, "");
		for(DeclarationType declarationType : source.getDeclarationTypes()){
			declarationTypeMap.put(declarationType.getId(), declarationType.getName());
		}
		return declarationTypeMap;
	}

	private void detectUserRoles(){
		DetectUserRoleAction action = new DetectUserRoleAction();
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<DetectUserRoleResult>() {
					@Override
					public void onSuccess(DetectUserRoleResult result) {
                    }
				}, this));
	}

}
