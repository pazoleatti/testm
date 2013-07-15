package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class UpdateFormSourcesAction extends UnsecuredActionImpl<UpdateFormSourcesResult> {
	private Long departmentFormTypeId;
	private List<Long> sourceDepartmentFormTypeIds;

	public Long getDepartmentFormTypeId() {
		return departmentFormTypeId;
	}

	public void setDepartmentFormTypeId(Long departmentFormTypeId) {
		this.departmentFormTypeId = departmentFormTypeId;
	}

	public List<Long> getSourceDepartmentFormTypeIds() {
		return sourceDepartmentFormTypeIds;
	}

	public void setSourceDepartmentFormTypeIds(List<Long> sourceDepartmentFormTypeIds) {
		this.sourceDepartmentFormTypeIds = sourceDepartmentFormTypeIds;
	}
}
