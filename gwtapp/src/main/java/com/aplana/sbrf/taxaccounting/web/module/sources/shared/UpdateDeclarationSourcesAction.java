package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class UpdateDeclarationSourcesAction extends UnsecuredActionImpl<UpdateSourcesResult> {
	private Integer departmentDeclarationTypeId;
	private List<Long> sourceDepartmentFormTypeIds;

	public Integer getDepartmentDeclarationTypeId() {
		return departmentDeclarationTypeId;
	}

	public void setDepartmentDeclarationTypeId(Integer departmentDeclarationTypeId) {
		this.departmentDeclarationTypeId = departmentDeclarationTypeId;
	}

	public List<Long> getSourceDepartmentFormTypeIds() {
		return sourceDepartmentFormTypeIds;
	}

	public void setSourceDepartmentFormTypeIds(List<Long> sourceDepartmentFormTypeIds) {
		this.sourceDepartmentFormTypeIds = sourceDepartmentFormTypeIds;
	}
}
