package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormDataList extends UnsecuredActionImpl<GetFormDataListResult>{

    private FormDataFilter formDataFilter;
	private TaxType taxType;

    public GetFormDataList() {

    }

    public FormDataFilter getFormDataFilter(){
        return formDataFilter;
    }

    public void setFormDataFilter(FormDataFilter formDataFilter){
        this.formDataFilter = formDataFilter;
    }

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

}
