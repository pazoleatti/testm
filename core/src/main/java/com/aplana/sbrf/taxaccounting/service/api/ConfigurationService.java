package com.aplana.sbrf.taxaccounting.service.api;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;
import java.util.Map;

public interface ConfigurationService {

    /**
     * Получение конфигурационных параметров (табл. CONFIGURATION)
     */
    ConfigurationParamModel getAllConfig(TAUserInfo userInfo);

    /**
     * Получает настройки почты
     * @return настройки почты - список <Столбец, Значение>
     */
    List<Map<String, String>> getEmailConfig();

    /**
     * Получает настройки асинхронных задач
     * @return настройки асинхронных задач - список <Столбец, Значение>
     */
    List<Map<String, String>> getAsyncConfig();

    /**
     * Получение конф.параметров по подразделению
     * @param departmentId идентификатор подразделения
     * @return модель
     */
    ConfigurationParamModel getByDepartment(Integer departmentId, TAUserInfo userInfo);

    /**
     * Сохранение конфигурационных параметров (табл. CONFIGURATION)
     */
	void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, List<Map<String, String>> emailConfigs, List<Map<String, String>> asyncConfigs, Logger logger);

    /**
     * Проверка доступности путей в указанных конфигурационных параметрах (табл. CONFIGURATION)
     */
    void checkReadWriteAccess(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger);

    /**
     * Проверка общих параметров {@link ConfigurationParamGroup#COMMON_PARAM}
     *
     * @return список параметров с ошибками
     */
    List<ConfigurationParam> checkCommonConfigurationParams(Map<ConfigurationParam, String> configurationParamMap, Logger logger);

    /**
     * Сохранение общих параметров {@link ConfigurationParamGroup#COMMON_PARAM}
     */
    void saveCommonConfigurationParams(Map<ConfigurationParam, String> configurationParamMap);
}
