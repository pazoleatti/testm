package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Запрос для заполнения полей формы создания бух отчетности.
 * @author lhaziev
 */
public class BookerStatementsFieldsAction extends UnsecuredActionImpl<BookerStatementsFieldsResult>{

	private Long departmentId;
	private List<Integer> reportPeriodId;

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public List<Integer> getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(List<Integer> reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}
}
