package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFilterDataResult;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter.SelectItem;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.ArrayList;
import java.util.List;

public class FilterPresenter extends PresenterWidget<FilterPresenter.MyView> {

	public interface MyView extends View {
		void setDataFilter(FormDataFilter formDataFilter);

		FormDataFilter getDataFilter();

		void setPeriodList(List<SelectItem> list);

		void setDepartmentList(List<SelectItem> list);

		void setFormtypeList(List<SelectItem> list);

		public void setKindList(List<SelectItem> list);

		public void clearData();

	}

	private final DispatchAsync dispatchAsync;

	@Inject
	public FilterPresenter(EventBus eventBus, MyView view,
			DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
	}

	public FormDataFilter getFilterData() {
		return getView().getDataFilter();
	}

	public void initFilter(TaxType taxType) {
		GetFilterData action = new GetFilterData();
		action.setTaxType(taxType);
		dispatchAsync.execute(action,
				new AbstractCallback<GetFilterDataResult>() {
					@Override
					public void onSuccess(GetFilterDataResult result) {
						getView().clearData();
						List<SelectItem> reportPeriodItems = fillPeriodList(result);
						List<SelectItem> departmentItems = fillDepartmentList(result);
						List<SelectItem> formTypeItems = fillFormTypeList(result);

						List<SelectItem> type = new ArrayList<SelectItem>();
						type.add(new SelectItem(1L, "Сводная"));

						getView().setFormtypeList(type);
						getView().setPeriodList(reportPeriodItems);
						getView().setDepartmentList(departmentItems);
						getView().setKindList(formTypeItems);

						FormDataFilter formDataFilter = new FormDataFilter();
						getView().setDataFilter(formDataFilter);

						FilterReadyEvent.fire(FilterPresenter.this);
						super.onSuccess(result);
					}
				});

	}

	private List<SelectItem> fillPeriodList(GetFilterDataResult result){
		List<ReportPeriod> reportPeriods = result.getPeriods();
		List<SelectItem> reportPeriodItems = new ArrayList<SelectItem>();
		for (ReportPeriod reportPeriod : reportPeriods){
			reportPeriodItems.add(new SelectItem(Long
					.valueOf(reportPeriod.getId()),
					reportPeriod.getName()));
		}
		return reportPeriodItems;
	}

	private List<SelectItem> fillFormTypeList(GetFilterDataResult result){
		List<FormType> formTypes = result.getKinds();
		List<SelectItem> formTypeItems = new ArrayList<SelectItem>();
		for (FormType formType : formTypes) {
			formTypeItems.add(new SelectItem(Long
					.valueOf(formType.getId()), formType
					.getName()));
		}
		return formTypeItems;
	}

	private List<SelectItem> fillDepartmentList(GetFilterDataResult result){
		List<Department> departments = result.getDepartments();
		List<SelectItem> departmentItems = new ArrayList<SelectItem>();
		// TODO: переписать код с применением google Function()
		for (Department department : departments) {
			departmentItems.add(new SelectItem(Long
					.valueOf(department.getId()), department
					.getName()));
		}
		return departmentItems;
	}

}
