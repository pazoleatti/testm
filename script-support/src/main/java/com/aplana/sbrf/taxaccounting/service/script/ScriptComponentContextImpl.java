package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.TAUser;

public class ScriptComponentContextImpl implements ScriptComponentContext {
	
	private TAUser user;
	private String ip;

	public void setUser(TAUser user) {
		this.user = user;
	}

	@Override
	public TAUser getUser() {
		return user;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
