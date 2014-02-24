package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetFormDataListResult implements Result {
	private static final long serialVersionUID = -7862353388101445807L;
	
	private List<FormDataSearchResultItem> records;
	private List<Department> departments;
	private List<ReportPeriod> reportPeriods;
    Map<Integer, String> departmentFullNames;

	//общее количество записей (на всех страницах)
	private long totalCountOfRecords;
	
	public GetFormDataListResult() {
		
	}
	
	public GetFormDataListResult(List<FormDataSearchResultItem> records) {
		this.records = records;
	}
	
	public List<FormDataSearchResultItem> getRecords() {
		return records;
	}

	public void setRecords(List<FormDataSearchResultItem> records) {
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

    public Map<Integer, String> getDepartmentFullNames() {
        return departmentFullNames;
    }

    public void setDepartmentFullNames(Map<Integer, String> departmentFullNames) {
        this.departmentFullNames = departmentFullNames;
    }

    public long getTotalCountOfRecords() {
		return totalCountOfRecords;
	}

	public void setTotalCountOfRecords(long totalCountOfRecords) {
		this.totalCountOfRecords = totalCountOfRecords;
	}
}