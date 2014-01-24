package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDeadlineDepartmentsAction extends UnsecuredActionImpl<GetDeadlineDepartmentsResult> {
	TaxType taxType;
	DepartmentPair department;

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public DepartmentPair getDepartment() {
		return department;
	}

	public void setDepartment(DepartmentPair department) {
		this.department = department;
	}
}
