package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CreateDeclaration extends UnsecuredActionImpl<CreateDeclarationResult> implements ActionName {

	public CreateDeclaration() {
	}

	private Integer declarationTypeId;

	private Integer reportPeriodId;

	private Integer departmentId;

    private Integer pagesCount;

	public Integer getDeclarationTypeId() {
		return declarationTypeId;
	}

	public void setDeclarationTypeId(Integer declarationTypeId) {
		this.declarationTypeId = declarationTypeId;
	}

	public Integer getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(Integer reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

    public Integer getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(Integer pagesCount) {
        this.pagesCount = pagesCount;
    }

    @Override
	public String getName() {
		return "Создание декларации";
	}
}
