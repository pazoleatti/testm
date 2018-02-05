package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTypeAssignment;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class DeleteDeclarationSourcesAction extends UnsecuredActionImpl<DeleteDeclarationSourcesResult> {
	private int departmentId;
	private int declarationTypeId;
	List<DeclarationTypeAssignment> kind;

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public int getDeclarationTypeId() {
		return declarationTypeId;
	}

	public void setDeclarationTypeId(int declarationTypeId) {
		this.declarationTypeId = declarationTypeId;
	}

	public List<DeclarationTypeAssignment> getKind() {
		return kind;
	}

	public void setKind(List<DeclarationTypeAssignment> kind) {
		this.kind = kind;
	}
}
