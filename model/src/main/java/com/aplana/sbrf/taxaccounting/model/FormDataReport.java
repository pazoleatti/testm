package com.aplana.sbrf.taxaccounting.model;

public class FormDataReport {
	
	private FormData data;
	private FormTemplate formTemplate;
	private Department department;
	private ReportPeriod reportPeriod;
	
	public FormData getData() {
		return data;
	}
	public void setData(FormData data) {
		this.data = data;
	}
	public FormTemplate getFormTemplate() {
		return formTemplate;
	}
	public void setFormTemplate(FormTemplate formTemplate) {
		this.formTemplate = formTemplate;
	}
	public Department getDepartment() {
		return department;
	}
	public void setDepartment(Department department) {
		this.department = department;
	}
	public ReportPeriod getReportPeriod() {
		return reportPeriod;
	}
	public void setReportPeriod(ReportPeriod reportPeriod) {
		this.reportPeriod = reportPeriod;
	}
	
	
}
