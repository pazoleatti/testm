package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class AddDeclarationSourceAction  extends UnsecuredActionImpl<AddDeclarationSourceResult> {
    private List<Integer> departmentId;
    private List<Long> declarationTypeId;
    private TaxType taxType;
    private List<Integer> performers;

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

    public List<Integer> getPerformers() {
        return performers;
    }

    public void setPerformers(List<Integer> performers) {
        this.performers = performers;
    }
}
