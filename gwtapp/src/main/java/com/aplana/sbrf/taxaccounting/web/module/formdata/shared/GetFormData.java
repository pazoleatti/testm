package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

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
	
	private Integer departmentId;

    private Long formDataKind;

	private boolean lockFormData;

	public Long getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(Long formDataId) {
		this.formDataId = formDataId;
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

	public boolean isLockFormData() {
		return lockFormData;
	}

	public void setLockFormData(boolean lockFormData) {
		this.lockFormData = lockFormData;
	}

}
