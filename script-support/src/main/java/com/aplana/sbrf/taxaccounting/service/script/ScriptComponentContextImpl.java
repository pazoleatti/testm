package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.TAUser;


/**
 * Реализация контекста для сомпонентов вызываемых из скриптов
 * 
 * @author sgoryachkin
 *
 */
public class ScriptComponentContextImpl implements ScriptComponentContext {
	
	private TAUser user;
	private Logger logger;
	private String ip;

	public void setUser(TAUser user) {
		this.user = user;
	}

	@Override
	public TAUser getUser() {
		return user;
	}
	
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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
