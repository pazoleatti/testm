package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFormDataListResult implements Result {
	private List<FormData> records;
	private List<Department> departments;
	private List<ReportPeriod> reportPeriods;
	
	public GetFormDataListResult() {
		
	}
	
	public GetFormDataListResult(List<FormData> records) {
		this.records = records;
	}
	
	public List<FormData> getRecords() {
		return records;
	}

	public void setRecords(List<FormData> records) {
		this.records = records;
	}

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}

	public List<ReportPeriod> getReportPeriods() {
		return reportPeriods;
	}

	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		this.reportPeriods = reportPeriods;
	}
}