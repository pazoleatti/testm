package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFilterDataResult implements Result {

	private static final long serialVersionUID = -4520077203875538525L;

	private Boolean isActive;
	private List<TARole> allRoles;

	public Boolean getActive() {
		return isActive;
	}

	public void setActive(Boolean active) {
		isActive = active;
	}

	public List<TARole> getAllRoles() {
		return allRoles;
	}

	public void setAllRoles(List<TARole> allRoles) {
		this.allRoles = allRoles;
	}
}
