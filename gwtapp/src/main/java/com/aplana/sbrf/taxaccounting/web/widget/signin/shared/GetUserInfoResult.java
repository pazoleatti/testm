package com.aplana.sbrf.taxaccounting.web.widget.signin.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetUserInfoResult implements Result{
	
	private String userName;
	private String roleAnddepartment;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRoleAnddepartment() {
		return roleAnddepartment;
	}

	public void setRoleAnddepartment(String department) {
		this.roleAnddepartment = department;
	}
}
