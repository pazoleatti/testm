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
     * Получение конф.параметров по подразделению
     * @param departmentId идентификатор подразделения
     * @return модель
     */
    ConfigurationParamModel getByDepartment(Integer departmentId, TAUserInfo userInfo);

    /**
     * Сохранение конфигурационных параметров (табл. CONFIGURATION)
     */
	void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger);

    /**
     * Проверка доступности путей в указанных конфигурационных параметрах (табл. CONFIGURATION)
     */
    void checkReadWriteAccess(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger);
}
