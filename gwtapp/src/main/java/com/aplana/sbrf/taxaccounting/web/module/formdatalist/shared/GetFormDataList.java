package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.FilterData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class GetFormDataList extends UnsecuredActionImpl<GetFormDataListResult>{

    private FilterData filterData;
    private List<Long> departments;

    public GetFormDataList() {

    }

    public FilterData getFilterData(){
        return filterData;
    }

    public void setFilterData(FilterData filterData){
        this.filterData = filterData;
    }

    public List<Long> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Long> departments) {
        this.departments = departments;
    }
}
