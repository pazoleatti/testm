package com.aplana.sbrf.taxaccounting.service.impl.eventhandler;

import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

public interface EventHandler {
	
	int priority();
	
	void handle(TAUserInfo userInfo, FormData formData, FormDataEvent event, Logger logger,  Map<String, Object> additionalParameters);

}
