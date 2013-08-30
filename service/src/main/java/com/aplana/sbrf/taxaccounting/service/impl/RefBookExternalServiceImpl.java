package com.aplana.sbrf.taxaccounting.service.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.RefBookExternalService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;

@Service
@Transactional
public class RefBookExternalServiceImpl implements RefBookExternalService {
	
	@Autowired
	RefBookScriptingService refBookScriptingService;

	@Override
	public void importRefBook(TAUserInfo userInfo, Logger logger, Long refBookId, InputStream is) {
        Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("ImportInputStream", is);
		refBookScriptingService.executeScript(userInfo, refBookId, FormDataEvent.IMPORT, logger, null);
		if (logger.containsLevel(LogLevel.ERROR)){
				throw new ServiceLoggerException(
						"Произошли ошибки в скрипте импорта справочника",
						logger.getEntries());
		}
	}

}
