package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

public interface ScriptComponentContext {
	
	TAUserInfo getUserInfo();
	Logger getLogger();

}
