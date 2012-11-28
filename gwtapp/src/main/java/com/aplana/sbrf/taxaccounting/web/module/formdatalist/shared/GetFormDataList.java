package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormDataList extends UnsecuredActionImpl<GetFormDataListResult>{

    private FormDataFilter formDataFilter;

    public GetFormDataList() {

    }

    public FormDataFilter getFormDataFilter(){
        return formDataFilter;
    }

    public void setFormDataFilter(FormDataFilter formDataFilter){
        this.formDataFilter = formDataFilter;
    }

}
