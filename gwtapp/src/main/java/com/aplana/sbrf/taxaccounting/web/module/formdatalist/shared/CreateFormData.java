package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CreateFormData extends UnsecuredActionImpl<CreateFormDataResult> implements ActionName {

	private Integer formDataTypeId;

	private Integer reportPeriodId;

	private Integer departmentId;
	
	private Integer formDataKindId;


	public Integer getFormDataTypeId() {
		return formDataTypeId;
	}


	public void setFormDataTypeId(Integer formDataTypeId) {
		this.formDataTypeId = formDataTypeId;
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


	public Integer getFormDataKindId() {
		return formDataKindId;
	}


	public void setFormDataKindId(Integer formDataKindId) {
		this.formDataKindId = formDataKindId;
	}


	@Override
	public String getName() {
		return "Создание налоговой формы";
	}
}
