package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.DataFilter;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormDataList extends UnsecuredActionImpl<GetFormDataListResult>{

    private DataFilter dataFilter;

    public GetFormDataList() {

    }

    public DataFilter getDataFilter(){
        return dataFilter;
    }

    public void setDataFilter(DataFilter dataFilter){
        this.dataFilter = dataFilter;
    }
}
