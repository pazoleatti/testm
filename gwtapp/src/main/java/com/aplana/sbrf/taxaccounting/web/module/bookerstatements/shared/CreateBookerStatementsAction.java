package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.CreateFormDataResult;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CreateBookerStatementsAction extends UnsecuredActionImpl<CreateBookerStatementsResult> implements ActionName {

	private Integer bookerStatementsTypeId;
	private Integer reportPeriodId;
	private Integer departmentId;

	public Integer getBookerStatementsTypeId() {
		return bookerStatementsTypeId;
	}


	public void setBookerStatementsTypeId(Integer bookerStatementsTypeId) {
		this.bookerStatementsTypeId = bookerStatementsTypeId;
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

    @Override
	public String getName() {
		return "Создание бух. отчетности";
	}
}
