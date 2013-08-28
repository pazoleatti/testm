package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetCurrentSourcesForDeclaratonAction extends UnsecuredActionImpl<GetCurrentSourcesResult> {
	
    private int departmentId;
	private int declarationTypeId;

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
}
