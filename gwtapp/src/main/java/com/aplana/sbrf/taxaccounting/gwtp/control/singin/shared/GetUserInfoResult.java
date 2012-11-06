package com.aplana.sbrf.taxaccounting.gwtp.control.singin.shared;

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
