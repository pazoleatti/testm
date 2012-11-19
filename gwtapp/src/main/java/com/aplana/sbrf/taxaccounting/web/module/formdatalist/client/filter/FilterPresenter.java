package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FilterData;
import com.aplana.sbrf.taxaccounting.model.FormType;
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
import java.util.Date;
import java.util.List;


public class FilterPresenter extends PresenterWidget<FilterView>{
	
	public interface MyView extends View {
		void setFilterData(FilterData filterData);
		FilterData getFilterData();
		void setPeriodList(List<SelectItem> list);
		void setDepartmentList(List<SelectItem> list);
		void setFormtypeList(List<SelectItem> list);
        public void setKindList(List<SelectItem> list);
        public void clearData();

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


        dispatchAsync.execute(new GetFilterData(),
                new AbstractCallback<GetFilterDataResult>() {
                    @Override
                    public void onSuccess(GetFilterDataResult result) {
                        getView().clearData();
                        List<Department> departments = result.getDepartments();
                        List<SelectItem> departmentItems = new ArrayList<SelectItem>();
                        //TODO: переписать код с применением google Function()
                        for (Department department : departments) {
                            departmentItems.add(new SelectItem(Long.valueOf(department.getId()), department.getName()));
                        }

                        List<FormType> formTypes = result.getKinds();
                        List<SelectItem> formTypeItems = new ArrayList<SelectItem>();
                        for (FormType formType : formTypes) {
                            formTypeItems.add(new SelectItem(Long.valueOf(formType.getId()), formType.getName()));
                        }


                        List<SelectItem> type = new ArrayList<SelectItem>();
                        List<SelectItem> period = new ArrayList<SelectItem>();
                        Date date = new Date();
                        type.add(new SelectItem(1L, "Сводная"));
                        period.add(new SelectItem(1L, date.toString()));

                        getView().setFormtypeList(type);
                        getView().setPeriodList(period);
                        getView().setDepartmentList(departmentItems);
                        getView().setKindList(formTypeItems);

                        FilterData filterData = new FilterData();
                        getView().setFilterData(filterData);
                    }
                });

    }

}
