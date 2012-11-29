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
	//TODO: этот MAGIC_NUMBER уйдет когда разберемся с LongListBoxEditor, пока используем данную метку для обработки параметра ВСЕ
	private static final long MAGIC_NUMBER = Long.MAX_VALUE;

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
					public void onReqSuccess(GetFilterDataResult result) {
						getView().clearData();
						List<SelectItem> reportPeriodItems = fillPeriodList(result);
						List<SelectItem> departmentItems = fillDepartmentList(result);
						List<SelectItem> formTypeItems = fillFormTypeList(result);
						List<SelectItem> formKindList = fillFormKindList();

						getView().setFormtypeList(formTypeItems);
						getView().setPeriodList(reportPeriodItems);
						getView().setDepartmentList(departmentItems);
						getView().setKindList(formKindList);

						FormDataFilter formDataFilter = new FormDataFilter();
						getView().setDataFilter(formDataFilter);

						FilterReadyEvent.fire(FilterPresenter.this);
					}
				});

	}

	private List<SelectItem> fillPeriodList(GetFilterDataResult result){
		List<ReportPeriod> reportPeriods = result.getPeriods();
		List<SelectItem> reportPeriodItems = new ArrayList<SelectItem>();
		reportPeriodItems.add(new SelectItem(MAGIC_NUMBER, ""));
		for (ReportPeriod reportPeriod : reportPeriods){
			reportPeriodItems.add(new SelectItem((long) reportPeriod.getId(),
					reportPeriod.getName()));
		}
		return reportPeriodItems;
	}

	private List<SelectItem> fillFormTypeList(GetFilterDataResult result){
		List<FormType> formTypes = result.getFormTypes();
		List<SelectItem> formTypeItems = new ArrayList<SelectItem>();
		formTypeItems.add(new SelectItem(MAGIC_NUMBER, ""));
		for (FormType formType : formTypes) {
			formTypeItems.add(new SelectItem((long) formType.getId(), formType
					.getName()));
		}
		return formTypeItems;
	}

	private List<SelectItem> fillDepartmentList(GetFilterDataResult result){
		List<Department> departments = result.getDepartments();
		List<SelectItem> departmentItems = new ArrayList<SelectItem>();

		departmentItems.add(new SelectItem(MAGIC_NUMBER, ""));
		for (Department department : departments) {
			departmentItems.add(new SelectItem((long) department.getId(), department
					.getName()));
		}
		return departmentItems;
	}

	private List<SelectItem> fillFormKindList(){
		List<SelectItem> kind = new ArrayList<SelectItem>();
		kind.add(new SelectItem(MAGIC_NUMBER, ""));
		kind.add(new SelectItem(3L, "Сводная"));
		kind.add(new SelectItem(2L, "Консолидированная"));
		kind.add(new SelectItem(1L, "Первичная"));
		return kind;
	}

}
