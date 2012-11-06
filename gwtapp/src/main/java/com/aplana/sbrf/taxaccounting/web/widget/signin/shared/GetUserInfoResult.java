package com.aplana.sbrf.taxaccounting.web.widget.signin.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetUserInfoResult implements Result{
	
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
}
