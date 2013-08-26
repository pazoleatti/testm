package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;


/**
 * Реализация контекста для сомпонентов вызываемых из скриптов
 * 
 * @author sgoryachkin
 *
 */
public class ScriptComponentContextImpl implements ScriptComponentContext {
	
	private TAUserInfo userInfo;
	private Logger logger;

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/* (non-Javadoc)
	 * @see com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContext#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public TAUserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(TAUserInfo userInfo) {
		this.userInfo = userInfo;
	}
}
