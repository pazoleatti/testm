package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter.FilterData;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter.SelectItem;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;


public class FilterPresenter extends PresenterWidget<FilterView>{
	
	public interface MyView extends View {
		void setFilterData(FilterData filterData);
		FilterData getFilterData();
		void setPeriodList(List<SelectItem> list);
		void setDepartmentList(List<SelectItem> list);
		void setFormtypeList(List<SelectItem> list);
	}
	
	private final DispatchAsync dispatchAsync;
	
	@Inject
	public FilterPresenter(EventBus eventBus, FilterView view, DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
	}

	public FilterData getFilterData() {
		return getView().getFilterData();
	}
	
	@Override
	protected void onReveal() {			
		super.onReveal();
		List<SelectItem> items = new ArrayList<SelectItem>();
		items.add(new SelectItem(10l, "Ноль"));
		items.add(new SelectItem(21l, "Один"));
		items.add(new SelectItem(32l, "Два"));
		getView().setFormtypeList(items);
		getView().setDepartmentList(items);
		getView().setPeriodList(items);
		FilterData filterData = new FilterData();
		filterData.setDepartment(Arrays.asList(new Long[]{10l}));
		filterData.setPeriod(Arrays.asList(new Long[]{21l}));
		filterData.setFormtype(Arrays.asList(new Long[]{10l, 32l}));
		getView().setFilterData(filterData);
		System.out.println(getView().getFilterData().getFormtype().toString());
	}

}
