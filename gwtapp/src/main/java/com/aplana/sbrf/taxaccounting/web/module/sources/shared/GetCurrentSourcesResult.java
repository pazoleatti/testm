package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentFormTypeShared;
import com.gwtplatform.dispatch.shared.Result;

public class GetCurrentSourcesResult implements Result {
	private static final long serialVersionUID = 7613228265079535068L;
	
	private List<DepartmentFormTypeShared> currentSources;

	public List<DepartmentFormTypeShared> getCurrentSources() {
		return currentSources;
	}

	public void setCurrentSources(List<DepartmentFormTypeShared> currentSources) {
		this.currentSources = currentSources;
	}

}
