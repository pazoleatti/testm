package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import java.util.List;
import java.util.Map;

public interface ConfigurationService {

    /**
     * Получение конфигурационных параметров (табл. CONFIGURATION)
     */
    @Deprecated
    ConfigurationParamModel fetchAllConfig(TAUserInfo userInfo);

    /**
     * Получение конфигурационных параметров (табл. CONFIGURATION)
     *
     * @param userInfo информация о текущем пользователе
     * @return модель {@link ConfigurationParamModel} содержащаяя информацию о всех конфигурационных параметрах
     */
    ConfigurationParamModel getCommonConfig(TAUserInfo userInfo);

    /**
     * Получает настройки почты
     *
     * @return настройки почты - список <Столбец, Значение>
     */
    @Deprecated
    List<Map<String, String>> getEmailConfig();

    /**
     * Получает настройки асинхронных задач
     *
     * @return настройки асинхронных задач - список <Столбец, Значение>
     */
    @Deprecated
    List<Map<String, String>> getAsyncConfig();

    /**
     * Получение конф.параметров по подразделению
     *
     * @param departmentId идентификатор подразделения
     * @return модель
     */
    ConfigurationParamModel fetchAllByDepartment(Integer departmentId, TAUserInfo userInfo);

    /**
     * Сохранение конфигурационных параметров (табл. CONFIGURATION)
     */
    @Deprecated
    void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, List<Map<String, String>> emailConfigs, List<Map<String, String>> asyncConfigs, Logger logger);

    /**
     * Проверка доступности путей для чтения или записи в указанном параметре
     *
     * @param userInfo информация о пользователе
     * @param param    конфигурационный параметр
     * @return uuid идентификатор логгера
     */
    String checkReadWriteAccess(TAUserInfo userInfo, Configuration param);

    /**
     * Проверка доступности путей в указанных конфигурационных параметрах (табл. CONFIGURATION)
     */
    @Deprecated
    void checkReadWriteAccess(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger);

    /**
     * Проверка общих параметров {@link ConfigurationParamGroup#COMMON_PARAM}
     *
     * @return список параметров с ошибками
     */
    @Deprecated
    List<ConfigurationParam> checkCommonConfigurationParams(Map<ConfigurationParam, String> configurationParamMap, Logger logger);

    /**
     * Сохранение общих параметров {@link ConfigurationParamGroup#COMMON_PARAM}
     */
    @Deprecated
    void saveCommonConfigurationParams(Map<ConfigurationParam, String> configurationParamMap, TAUserInfo userInfo);

    /**
     * Установить значение общих конфигурационных параметров по умолчанию (табл. CONFIGURATION)
     *
     * @param userInfo информация о пользователе
     */
    void setCommonParamsDefault(TAUserInfo userInfo);

    /**
     * Получение списка типов асинхронных задач
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link PagingResult} с данными {@link AsyncTaskTypeData}
     */
    PagingResult<AsyncTaskTypeData> fetchAllAsyncParam(PagingParams pagingParams);

    /**
     * Возвращяет страницу общих конфигурационных параметров
     *
     * @param pagingParams параметры пагинации
     * @param configurationParamGroup
     * @return страница {@link Configuration}
     */
    PagingResult<Configuration> fetchAllCommonParam(PagingParams pagingParams, ConfigurationParamGroup configurationParamGroup);

    /**
     * Создание нового значения конфигурацинного параметра
     *
     * @param commonParam конфигурационный параметр типа "Общие параметры"
     * @param userInfo    информация текущего польователя
     * @return uuid идентификатор логгера
     */
    String create(Configuration commonParam, TAUserInfo userInfo);

    /**
     * Полученние зачений параметров "Общие параметры", которых нет в БД
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link PagingResult} с данными {@link Configuration}
     */
    PagingResult<Configuration> fetchAllNonChangedCommonParam(PagingParams pagingParams);

    /**
     * Удаление записей конфигураций "Общие параметры"
     *
     * @param names    названия удаляемых параметров
     * @param userInfo информация о текущем пользователе
     * @return uuid идентификатор логгера
     */
    String remove(List<String> names, TAUserInfo userInfo);

    /**
     * Обновление записей конфигурационных параметров
     *
     * @param commonParam конфигурационные параметры "Общие параметры"
     * @param userInfo
     * @return uuid идентификатор логгера
     */
    String updateCommonParam(Configuration commonParam, TAUserInfo userInfo);

    /**
     * Обновление записей конфигурационных параметров
     *
     * @param asyncParam конфигурационные параметры "Параметры асинхронных задач"
     * @param userInfo
     * @return uuid идентификатор логгера
     */
    String updateAsyncParam(AsyncTaskTypeData asyncParam, TAUserInfo userInfo);
}