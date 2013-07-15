package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormSourcesAction extends UnsecuredActionImpl<GetFormSourcesResult> {
    private int departmentId;
	private TaxType taxType;

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
}
