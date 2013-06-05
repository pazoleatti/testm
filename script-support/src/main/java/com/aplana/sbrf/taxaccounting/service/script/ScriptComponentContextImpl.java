package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;


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
