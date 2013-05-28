package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.TAUser;

public class ScriptComponentContextImpl implements ScriptComponentContext {
	
	private TAUser user;

	public void setUser(TAUser user) {
		this.user = user;
	}

	@Override
	public TAUser getUser() {
		return user;
	}

}
