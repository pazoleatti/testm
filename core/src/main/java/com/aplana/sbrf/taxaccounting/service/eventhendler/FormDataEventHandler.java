package com.aplana.sbrf.taxaccounting.service.eventhendler;

import java.util.Map;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

public interface FormDataEventHandler {
	
	void handle(TAUserInfo userInfo, FormData formData, FormDataEvent event, Logger logger,  Map<String, Object> additionalParameters);

}
