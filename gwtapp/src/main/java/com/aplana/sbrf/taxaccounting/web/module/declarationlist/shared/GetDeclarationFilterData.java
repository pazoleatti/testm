package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;


import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDeclarationFilterData extends UnsecuredActionImpl<GetDeclarationFilterDataResult> {

	public GetDeclarationFilterData(){

	}

	private TaxType taxType;

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
}
