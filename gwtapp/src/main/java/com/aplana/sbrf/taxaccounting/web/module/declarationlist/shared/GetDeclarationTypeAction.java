package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class GetDeclarationTypeAction extends UnsecuredActionImpl<GetDeclarationTypeResult> {
	private int departmentId;
    private int reportPeriod;
    private TaxType taxType;
    private List<DeclarationFormKind> declarationFormKindList;

    public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public int getReportPeriod() {
		return reportPeriod;
	}

	public void setReportPeriod(int reportPeriod) {
		this.reportPeriod = reportPeriod;
	}

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public List<DeclarationFormKind> getDeclarationFormKindList() {
		return declarationFormKindList;
	}

	public void setDeclarationFormKindList(List<DeclarationFormKind> declarationFormKindList) {
		this.declarationFormKindList = declarationFormKindList;
	}
}
