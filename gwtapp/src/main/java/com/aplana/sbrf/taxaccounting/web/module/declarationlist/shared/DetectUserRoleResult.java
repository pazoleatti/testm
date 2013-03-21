package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class DetectUserRoleResult implements Result {

	List<TARole> userRole;

	public List<TARole> getUserRole() {
		return userRole;
	}

	public void setUserRole(List<TARole> userRole) {
		this.userRole = userRole;
	}
}
