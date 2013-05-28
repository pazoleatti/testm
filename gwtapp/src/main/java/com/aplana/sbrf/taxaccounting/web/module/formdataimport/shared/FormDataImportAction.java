package com.aplana.sbrf.taxaccounting.web.module.formdataimport.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class FormDataImportAction extends UnsecuredActionImpl<FormDataImportResult>{
	
	private int formTemplateId;
	private int departmentId;
	private FormDataKind kind;
	private int reportPeriodId;
	
	public int getFormTemplateId() {
		return formTemplateId;
	}
	public void setFormTemplateId(int formTemplateId) {
		this.formTemplateId = formTemplateId;
	}
	public int getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}
	public FormDataKind getKind() {
		return kind;
	}
	public void setKind(FormDataKind kind) {
		this.kind = kind;
	}
	public int getReportPeriodId() {
		return reportPeriodId;
	}
	public void setReportPeriodId(int reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	
}
