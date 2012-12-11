package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormData extends UnsecuredActionImpl<GetFormDataResult> {
	private Long formDataId;
	
	private List<LogEntry> logEntries;
	
	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		this.logEntries = logEntries;
	}

	private WorkflowMove workFlowMove;

	private Long formDataTypeId;
	
	private Long reportPeriodId;

	private Integer departmentId;

    private Long formDataKind;

	public Long getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(Long formDataId) {
		this.formDataId = formDataId;
	}

	public Long getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(Long reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public Long getFormDataKind() {
		return formDataKind;
	}

	public void setFormDataKind(Long formDataKind) {
		this.formDataKind = formDataKind;
	}

	public Long getFormDataTypeId() {
		return formDataTypeId;
	}

	public void setFormDataTypeId(Long formDataTypeId) {
		this.formDataTypeId = formDataTypeId;
	}


	public WorkflowMove getWorkFlowMove() {
		return workFlowMove;
	}

	public void setWorkFlowMove(WorkflowMove workFlowMove) {
		this.workFlowMove = workFlowMove;
	}
}
