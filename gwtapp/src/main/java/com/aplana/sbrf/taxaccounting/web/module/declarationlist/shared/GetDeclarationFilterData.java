package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;


import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDeclarationFilterData extends UnsecuredActionImpl<GetDeclarationFilterDataResult> {

	private TaxType taxType;
    private boolean isReports;

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

    public boolean isReports() {
        return isReports;
    }

    public void setReports(boolean isReports) {
        this.isReports = isReports;
    }
}
