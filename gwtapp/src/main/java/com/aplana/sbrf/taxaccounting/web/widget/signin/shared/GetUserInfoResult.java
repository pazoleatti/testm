package com.aplana.sbrf.taxaccounting.web.widget.signin.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetUserInfoResult implements Result{
	
	private String userName;
	private String roleAnddepartment;
    private String hint;

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

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
