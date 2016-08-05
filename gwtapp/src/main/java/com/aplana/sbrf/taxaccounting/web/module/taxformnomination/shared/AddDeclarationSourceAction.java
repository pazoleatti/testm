package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class AddDeclarationSourceAction  extends UnsecuredActionImpl<AddDeclarationSourceResult> {
	List<Integer> departmentId;
	List<Long> declarationTypeId;
	TaxType taxType;

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public List<Integer> getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(List<Integer> departmentId) {
		this.departmentId = departmentId;
	}

	public List<Long> getDeclarationTypeId() {
		return declarationTypeId;
	}

	public void setDeclarationTypeId(List<Long> declarationTypeId) {
		this.declarationTypeId = declarationTypeId;
	}
}
