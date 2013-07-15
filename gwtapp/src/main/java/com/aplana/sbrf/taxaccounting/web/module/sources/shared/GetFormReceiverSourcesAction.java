package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormReceiverSourcesAction extends UnsecuredActionImpl<GetFormReceiverSourcesResult> {
    private int departmentId;
	private int formTypeId;
	private FormDataKind kind;

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public int getFormTypeId() {
		return formTypeId;
	}

	public void setFormTypeId(int formTypeId) {
		this.formTypeId = formTypeId;
	}

	public FormDataKind getKind() {
		return kind;
	}

	public void setKind(FormDataKind kind) {
		this.kind = kind;
	}
}
