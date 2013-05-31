package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.TAUser;

public interface ScriptComponentContext {
	
	TAUser getUser();
	String getIp();
}
