package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import java.util.List;
import java.util.Map;

@ScriptExposed
public interface ConfigurationService {

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
    List<Configuration> getEmailConfig();

    /**
     * Получение конфигурациооных параметров по подразделению
     *
     * @param departmentId идентификатор подразделения
     * @return модель {@link ConfigurationParamModel} содержащаяя информацию о конфигурационных параметрах
     */
    ConfigurationParamModel fetchAllByDepartment(Integer departmentId, TAUserInfo userInfo);

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
     * Возвращает общий параметр конфигурации по перечислению {@link ConfigurationParam}
     *
     * @param param перечисление {@link ConfigurationParam}
     * @return параметр {@link Configuration} или null, если не найден
     */
    Configuration fetchByEnum(ConfigurationParam param);

    /**
     * Возвращает целочисленное значение параметра.
     *
     * @return целочисленное значение параметра, либо null в остальных случаях.
     */
    Integer getParamIntValue(ConfigurationParam param);

    /**
     * Возвращяет map вида код-параметр для определенного списка кодов
     *
     * @param params   список параметров
     * @param userInfo пользователь
     * @return map в виде код-параметр
     */
    Map<String, Configuration> fetchAllByEnums(List<ConfigurationParam> params, TAUserInfo userInfo);

    /**
     * То же что {@link #fetchAllByEnums(List, TAUserInfo)}, но без проверки прав
     */
    Map<String, Configuration> fetchAllByEnums(List<ConfigurationParam> params);

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
     * Сохранение конфигурационных параметров электронной почты
     *
     * @param emailParam конфигурационный параметр
     * @param userInfo   информация о пользователе
     * @return uuid идентификатор логгера
     */
    String updateEmailParam(Configuration emailParam, TAUserInfo userInfo);

    /**
     * Проверка конфигурационного параметра на валидность введенных значений и доступа к чтению/записи пути фаловой системы,
     * указанного в параметре
     *
     * @param param    проверяемый параметр
     * @param userInfo информация о пользователе
     * @return uuid идентификатор логгера
     */
    String checkConfigParam(Configuration param, TAUserInfo userInfo);

    /**
     * Получение конфигурационных параметров электронной почты
     *
     * @param pagingParams параметры пагинации
     * @param userInfo     информация о пользователе
     * @return страница {@link PagingResult} с данными {@link Configuration}
     */
    PagingResult<Configuration> fetchEmailParams(PagingParams pagingParams, TAUserInfo userInfo);

    /**
     * Получение конфигурционных параметров электронной почты, необходимых для авторизации почтового клиента
     *
     * @return мапа <название, значение> с данными {@link Configuration}
     */
    Map<String, String> fetchAuthEmailParamsMap();

    /**
     * Проверка числа на соответствие параметру "Максимальное количество строк РНУ для массового изменения"
     * @param count проверяемое число
     * @return ActionResult со статусом и логами проверки
     */
    ActionResult checkRowsEditCountParam(int count);
}