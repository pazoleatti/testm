package com.aplana.sbrf.taxaccounting.service.api;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamGroup;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;

import java.util.List;
import java.util.Map;

public interface ConfigurationService {

    /**
     * Получение конфигурационных параметров (табл. CONFIGURATION)
     */
    ConfigurationParamModel getAllConfig(TAUserInfo userInfo);

    /**
     * Получение конфигурационных параметров (табл. CONFIGURATION)
     */
    ConfigurationParamModel getCommonConfig(TAUserInfo userInfo);

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
     * Получение конф.параметров по коду
     * @param code
     * @return модель
     */
    ConfigurationParamModel get(String code);

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
    void saveCommonConfigurationParams(Map<ConfigurationParam, String> configurationParamMap, TAUserInfo userInfo);

    /**
     * Валидация расписания
     * @param schedule
     * @return
     */
    boolean validateSchedule(String schedule);

    /**
     * Получение параметров задачи планировщика
     * @param task
     * @return
     */
    SchedulerTaskData getSchedulerTask(SchedulerTask task);

    /**
     * Получение параметров всех задач планировщика
     * @return
     */
    List<SchedulerTaskData> getAllSchedulerTask();

    /**
     * Изменение признака активности задач
     * @param active
     * @param ids
     */
    void setActiveSchedulerTask(boolean active, List<Long> ids);

    /**
     * Обновить дату последнего запуска задачи
     * @param task
     */
    void updateTaskStartDate(SchedulerTask task);

    /**
     * Обновление параметров задачи планировщика
     */
    void updateTask(SchedulerTaskData taskData);
}
