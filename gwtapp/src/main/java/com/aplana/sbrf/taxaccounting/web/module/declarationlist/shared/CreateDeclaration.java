package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CreateDeclaration extends UnsecuredActionImpl<CreateDeclarationResult> implements ActionName {

	public CreateDeclaration() {
	}

	private Integer declarationTypeId;

	private Integer departmentReportPeriodId;

    private TaxType taxType;

    private String taxOrganCode;

    private String taxOrganKpp;

	public Integer getDeclarationTypeId() {
		return declarationTypeId;
	}

	public void setDeclarationTypeId(Integer declarationTypeId) {
		this.declarationTypeId = declarationTypeId;
	}

    public Integer getDepartmentReportPeriodId() {
        return departmentReportPeriodId;
    }

    public void setDepartmentReportPeriodId(Integer departmentReportPeriodId) {
        this.departmentReportPeriodId = departmentReportPeriodId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public String getTaxOrganCode() {
        return taxOrganCode;
    }

    public void setTaxOrganCode(String taxOrganCode) {
        this.taxOrganCode = taxOrganCode;
    }

    public String getTaxOrganKpp() {
        return taxOrganKpp;
    }

    public void setTaxOrganKpp(String taxOrganKpp) {
        this.taxOrganKpp = taxOrganKpp;
    }

    @Override
	public String getName() {
		return "Создание декларации";
	}
}
