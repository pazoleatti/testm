package com.aplana.sbrf.taxaccounting.service.script;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public interface ScriptComponentContextHolder {
	
	void setScriptComponentContext(ScriptComponentContext context);

}
