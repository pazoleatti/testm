package com.aplana.sbrf.taxaccounting.service.api;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

public interface ConfigurationService {

    /**
     * Получение конфигурационных параметров (табл. CONFIGURATION)
     */
    ConfigurationParamModel getAllConfig(TAUserInfo userInfo);

    /**
     * Сохранение конфигурационных параметров (табл. CONFIGURATION)
     */
	void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger);
}
