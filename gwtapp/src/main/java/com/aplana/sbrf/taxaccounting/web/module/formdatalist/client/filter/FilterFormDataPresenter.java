package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterFormDataPresenter extends PresenterWidget<FilterFormDataPresenter.MyView> implements FilterFormDataUIHandlers {

	public interface MyView extends View, HasUiHandlers<FilterFormDataUIHandlers> {
		
		// Установка/получение значений фильтра
		
		void setDataFilter(FormDataFilter formDataFilter);

		FormDataFilter getDataFilter();
		
		// Установка доступных значений

		void setFormStateList(List<WorkflowState> list);

		void setReturnStateList(List<Boolean> list);

        void setCorrectionTagList(List<Boolean> list);

        void setFilter(String filter);

		void setDepartments(List<Department> list, Set<Integer> availableValues);

		void setReportPeriods(List<ReportPeriod> reportPeriods);

		void setElementNames(Map<FormDataElementName, String> names);

        // Установить фильтр для типов налоговых форм
        void setKindFilter(List<FormDataKind> dataKinds);

        void clean();

        void setReportPeriodType(String type);
        void setDefaultReportPeriod(List<ReportPeriod> reportPeriods);
	}

	private final DispatchAsync dispatchAsync;

	@Inject
	public FilterFormDataPresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	public FormDataFilter getFilterData() {
		return getView().getDataFilter();
	}

	public void initFilter(final TaxType taxType, final FormDataFilter filter, final GetKindListResult kindListResult) {
        getView().setReportPeriodType(taxType.name());
		GetFilterData action = new GetFilterData();
		action.setTaxType(taxType);
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onSuccess(GetFilterDataResult result) {
						FormDataFilterAvailableValues filterValues = result.getFilterValues();
                        getView().setKindFilter(kindListResult.getDataKinds());
						getView().setDepartments(result.getDepartments(), filterValues.getDepartmentIds());
                        getView().setFilter("TAX_TYPE='" + taxType.getCode() + "'");
						getView().setReportPeriods(result.getReportPeriods());
						getView().setFormStateList(Arrays.asList(WorkflowState.values()));
						getView().setReturnStateList(Arrays.asList(new Boolean[]{ Boolean.TRUE, Boolean.FALSE }));
                        getView().setCorrectionTagList(Arrays.asList(new Boolean[]{ Boolean.TRUE, Boolean.FALSE }));
						// Если при инициализации фильтра клиент устанавливает фильтр, то берем его.
						// Иначе устанавливаем фильтр по умолчанию.
						if (filter != null){
							getView().setDataFilter(filter);
						} else {
							getView().setDataFilter(result.getDefaultFilter());
                            getView().setDefaultReportPeriod(result.getReportPeriods());
						}
						FilterFormDataReadyEvent.fire(FilterFormDataPresenter.this, true);
					}
					
					@Override
					public void onFailure(Throwable caught) {
						super.onFailure(caught);
						FilterFormDataReadyEvent.fire(FilterFormDataPresenter.this, false);
					}
					
				}, this));
	}

	public void changeFilterElementNames(TaxType taxType) {
		GetFieldsNames action = new GetFieldsNames();
		action.setTaxType(taxType);
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFieldsNamesResult>() {
					@Override
					public void onSuccess(GetFieldsNamesResult result) {
						getView().setElementNames(result.getFieldNames());
					}
				}, this)
		);
	}

	@Override
	public void onApplyClicked() {
		FormDataListApplyEvent.fire(this);
	}

}
