package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.gwtplatform.dispatch.shared.Result;

public class GetCurrentAssignsResult implements Result {
	private static final long serialVersionUID = 7613228265079535068L;
	
	private List<CurrentAssign> currentSources;

	public List<CurrentAssign> getCurrentSources() {
		return currentSources;
	}

	public void setCurrentSources(List<CurrentAssign> currentSources) {
		this.currentSources = currentSources;
	}

}
