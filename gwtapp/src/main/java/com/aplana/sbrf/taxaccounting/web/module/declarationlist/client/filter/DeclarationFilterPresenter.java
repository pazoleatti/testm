package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterData;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetDeclarationFilterDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.*;

import static java.util.Arrays.asList;

public class DeclarationFilterPresenter extends PresenterWidget<DeclarationFilterPresenter.MyView>
		implements DeclarationFilterUIHandlers {

    @Override
    public void onApplyFilter() {
        DeclarationFilterApplyEvent.fire(this);
    }

	@Override
	public void onResetFilter() {
		DeclarationDataFilter dataFilter = new DeclarationDataFilter();
		dataFilter.setTaxType(declarationListPresenter.getTaxType());
		getView().setDataFilter(dataFilter);
	}

	public interface MyView extends View, HasUiHandlers<DeclarationFilterUIHandlers> {
		void setDataFilter(DeclarationDataFilter formDataFilter);

        DeclarationDataFilter getFilterData();

		void setDepartmentsList(List<Department> list, Set<Integer> availableDepartments);

        void setKindFilter(List<DeclarationFormKind> dataKinds);

		void setDeclarationTypeMap(Map<Integer, String> declarationTypeMap);

		void setReportPeriods(List<ReportPeriod> reportPeriods);

        void setFormStateList(List<State> list);

        void updateFilter(TaxType taxType, boolean isReports);

        void setCorrectionTagList(List<Boolean> list);

        void setReportPeriodType(String type);

        void setUserDepartmentId(Integer userDepartmentId);

		void setAsnuFilter(List<Long> asnuIds);

		void addEnterNativePreviewHandler();

		void removeEnterNativePreviewHandler();
    }

	private final DispatchAsync dispatchAsync;
	private DeclarationListPresenter declarationListPresenter;

    @Inject
	public DeclarationFilterPresenter(EventBus eventBus, MyView view,
	                                  DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	public DeclarationDataFilter getFilterData() {
		return getView().getFilterData();
	}

	public void initFilter(final TaxType taxType, final boolean isReports, final DeclarationDataFilter dataFilter) {

		GetDeclarationFilterData action = new GetDeclarationFilterData();
        action.setTaxType(taxType);
        action.setReports(isReports);

        // выставляем фильтр по умолчанию, иначе при задании размера пейджинга после получения nType
        // происходит обращение к driver.flush(), а driver.edit еще не выполнился т.к не отработал асинхронный вызов
        DeclarationDataFilter defaultFilter = new DeclarationDataFilter();
        defaultFilter.setTaxType(taxType);
        //getView().setDataFilter(defaultFilter);
        getView().setReportPeriodType(taxType.name());
        dispatchAsync.execute(action, CallbackUtils
				        .defaultCallback(new AbstractCallback<GetDeclarationFilterDataResult>() {
						@Override
						public void onSuccess(GetDeclarationFilterDataResult result) {
                            DeclarationDataFilterAvailableValues filterValues = result.getFilterValues();

							getView().setKindFilter(result.getDataKinds());
                            getView().setDepartmentsList(result.getDepartments(), filterValues.getDepartmentIds());
							getView().setReportPeriods(result.getPeriods());
							getView().setDeclarationTypeMap(fillDeclarationTypesMap(filterValues));
                            getView().setFormStateList(asList(null, State.CREATED, State.PREPARED, State.ACCEPTED));
                            getView().setCorrectionTagList(Arrays.asList(new Boolean[]{Boolean.TRUE, Boolean.FALSE}));
                            getView().setAsnuFilter(result.getAsnuIds());
                            if (dataFilter != null){
                                getView().setDataFilter(dataFilter);
                            } else {
                                getView().setDataFilter(result.getDefaultDecFilterData());
                                getView().setUserDepartmentId(result.getUserDepartmentId());
                            }
							DeclarationFilterReadyEvent.fire(DeclarationFilterPresenter.this);
						}

					    @Override
					    public void onFailure(Throwable caught) {
						    DeclarationFilterReadyEvent.fire(DeclarationFilterPresenter.this);
					    }
					}, this));
	}

	@Override
	protected void onReveal() {
		super.onReveal();
		getView().addEnterNativePreviewHandler();
	}

	@Override
	protected void onHide() {
		super.onHide();
		getView().removeEnterNativePreviewHandler();
	}

	private Map<Integer, String> fillDeclarationTypesMap(DeclarationDataFilterAvailableValues source){
		Map<Integer, String> declarationTypeMap = new LinkedHashMap<Integer, String>();
		for(DeclarationType declarationType : source.getDeclarationTypes()){
			declarationTypeMap.put(declarationType.getId(), declarationType.getName());
		}
		return declarationTypeMap;
	}

	public void setDeclarationListPresenter(DeclarationListPresenter declarationListPresenter) {
		this.declarationListPresenter = declarationListPresenter;
	}
}
