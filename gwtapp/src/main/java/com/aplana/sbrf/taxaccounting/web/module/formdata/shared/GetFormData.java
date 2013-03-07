package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.WorkflowMove;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.icommon.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormData extends UnsecuredActionImpl<GetFormDataResult> implements ActionName {
	
	private Long formDataId;

	private WorkflowMove workFlowMove;

	private Long formDataTypeId;

	private Integer departmentId;

	private Long formDataKind;

	private boolean readOnly;

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

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean lockFormData) {
		this.readOnly = lockFormData;
	}

	@Override
	public String getName() {
		return "\"Получение данных о выбранной налоговой форме\"";
	}


}
