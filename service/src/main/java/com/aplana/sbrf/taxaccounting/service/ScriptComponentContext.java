package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

public interface ScriptComponentContext {
	
	TAUserInfo getUserInfo();
	Logger getLogger();

}
