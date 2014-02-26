package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

public class FormDataReport {
	
	private FormData data;
	private FormTemplate formTemplate;
	private String departmentName;
	private ReportPeriod reportPeriod;
	private Date acceptanceDate;
	private Date creationDate;

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
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public ReportPeriod getReportPeriod() {
		return reportPeriod;
	}
	public void setReportPeriod(ReportPeriod reportPeriod) {
		this.reportPeriod = reportPeriod;
	}
	public Date getAcceptanceDate() {
		return acceptanceDate;
	}
	public void setAcceptanceDate(Date acceptanceDate) {
		this.acceptanceDate = acceptanceDate;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}
