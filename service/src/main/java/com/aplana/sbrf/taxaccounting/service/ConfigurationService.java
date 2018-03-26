package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;
import java.util.Map;

@ScriptExposed
public interface ConfigurationService {

    /**
     * Получение конфигурационных параметров (табл. CONFIGURATION)
     */
    @Deprecated
    ConfigurationParamModel fetchAllConfig(TAUserInfo userInfo);

    /**
     * Получение модели конфигурационных параметров, на просмотр которых пользователь имеет права
     *
     * @param userInfo информация о текущем пользователе
     * @return модель {@link ConfigurationParamModel} содержащаяя информацию о всех конфигурационных параметрах
     */
    ConfigurationParamModel getCommonConfig(TAUserInfo userInfo);

    /**
     * Получение модели конфигурационных параметров без проверки прав
     *
     * @return модель {@link ConfigurationParamModel} содержащаяя информацию о всех конфигурационных параметрах
     */
    ConfigurationParamModel getCommonConfigUnsafe();

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
     * Получение конфигурациооных параметров по подразделению
     *
     * @param departmentId идентификатор подразделения
     * @return модель {@link ConfigurationParamModel} содержащаяя информацию о конфигурационных параметрах
     */
    ConfigurationParamModel fetchAllByDepartment(Integer departmentId, TAUserInfo userInfo);

    /**
     * Сохранение конфигурационных параметров (табл. CONFIGURATION)
     */
    @Deprecated
    void saveAllConfig(TAUserInfo userInfo, ConfigurationParamModel model, List<Map<String, String>> emailConfigs, List<Map<String, String>> asyncConfigs, Logger logger);

    /**
     * Проверка конфигурационного параметра, представляющего из себя путь в файловой системе,
     * на наличие доступа на чтение/запись в зависимости от сути параметра
     *
     * @param param    конфигурационный параметр
     * @param userInfo информация о пользователе
     * @return uuid идентификатор логгера с результатом проверки
     */
    String checkFileSystemAccess(Configuration param, TAUserInfo userInfo);

    /**
     * Проверка доступности путей в указанных конфигурационных параметрах (табл. CONFIGURATION)
     */
    @Deprecated
    void checkFileSystemAccess(TAUserInfo userInfo, ConfigurationParamModel model, Logger logger);

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
     * Установка значений общих конфигурационных параметров по умолчанию
     *
     * @param userInfo информация о пользователе
     */
    void resetCommonParams(TAUserInfo userInfo);

    /**
     * Получение страницы типов асинхронных задач
     *
     * @param pagingParams параметры пагинации
     * @param userInfo     информация о пользователе
     * @return страница {@link PagingResult} с данными {@link AsyncTaskTypeData}
     */
    PagingResult<AsyncTaskTypeData> fetchAsyncParams(PagingParams pagingParams, TAUserInfo userInfo);

    /**
     * Получение страницы общих конфигурационных параметров
     *
     * @param pagingParams            параметры пагинации
     * @param configurationParamGroup группа параметров приложения, которые необходимо выгрузить
     * @param userInfo                информация о пользователе
     * @return страница {@link Configuration}
     */
    PagingResult<Configuration> fetchCommonParams(PagingParams pagingParams, ConfigurationParamGroup configurationParamGroup, TAUserInfo userInfo);

    /**
     * Создание конфигурацинного параметра
     *
     * @param commonParam конфигурационный параметр типа "Общие параметры"
     * @param userInfo    информация текущего польователя
     * @return uuid идентификатор логгера
     */
    String create(Configuration commonParam, TAUserInfo userInfo);

    /**
     * Полученние не созданых общих конфигурационных параметров
     *
     * @param pagingParams параметры пагинации
     * @param userInfo     информация о пользователе
     * @return страница {@link PagingResult} с данными {@link Configuration}
     */
    PagingResult<Configuration> fetchNonCreatedCommonParams(PagingParams pagingParams, TAUserInfo userInfo);

    /**
     * Удаление записей общих параметров конфигураций администрирования по коду
     *
     * @param codes    названия удаляемых параметров
     * @param userInfo информация о текущем пользователе
     * @return uuid идентификатор логгера
     */
    String remove(List<String> codes, TAUserInfo userInfo);

    /**
     * Сохранение общих конфигурационных параметров
     *
     * @param commonParam конфигурационный параметр
     * @param userInfo    информация о пользователе
     * @return uuid идентификатор логгера
     */
    String updateCommonParam(Configuration commonParam, TAUserInfo userInfo);

    /**
     * Сохранение конфигурационных параметров асинхронных задач
     *
     * @param asyncParam конфигурационный параметр
     * @param userInfo   информация о пользователе
     * @return uuid идентификатор логгера
     */
    String updateAsyncParam(AsyncTaskTypeData asyncParam, TAUserInfo userInfo);

    /**
     * Проверка конфигурационного параметра на валидность введенных значений и доступа к чтению/записи пути фаловой системы,
     * указанного в параметре
     *
     * @param param    проверяемый параметр
     * @param userInfo информация о пользователе
     * @return uuid идентификатор логгера
     */
    String checkConfigParam(Configuration param, TAUserInfo userInfo);
}