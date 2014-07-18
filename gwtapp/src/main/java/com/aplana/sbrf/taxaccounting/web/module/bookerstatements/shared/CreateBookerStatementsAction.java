package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CreateBookerStatementsAction extends UnsecuredActionImpl<CreateBookerStatementsResult> implements ActionName {

    private Integer year;
	private Long accountPeriodId;
	private Integer departmentId;
    private Integer bookerStatementsTypeId;

	public Integer getBookerStatementsTypeId() {
		return bookerStatementsTypeId;
	}


	public void setBookerStatementsTypeId(Integer bookerStatementsTypeId) {
		this.bookerStatementsTypeId = bookerStatementsTypeId;
	}


	public Long getAccountPeriodId() {
		return accountPeriodId;
	}


	public void setAccountPeriodId(Long accountPeriodId) {
		this.accountPeriodId = accountPeriodId;
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

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
