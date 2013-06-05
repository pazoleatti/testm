package com.aplana.sbrf.taxaccounting.service;

import java.util.Map;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

/**
 * Интерфейс сервиса для запуска скриптов по декларациями (пока существует только один скрипт - скрипт создания)
 * @author dsultanbekov
 */
public interface DeclarationDataScriptingService {
	
	void executeScript(TAUserInfo userInfo, DeclarationData declarationData, FormDataEvent event, Logger logger,  Map<String, Object> exchangeParams);

}
