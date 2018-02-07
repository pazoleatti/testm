package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;
import java.util.Map;

/**
 * ДАО для работы с параметрами приложения
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 10.10.13 11:34
 */

public interface ConfigurationDao {

    /**
     * Возвращяет {@link Configuration} по перечислению {@link ConfigurationParam}
     *
     * @param param перечисление {@link ConfigurationParam}
     * @return параметр {@link Configuration} или null, если не найден
     */
    Configuration fetchByEnum(ConfigurationParam param);

    /**
     * Возвращяет список всех конфигурационных параметров
     */
    List<Configuration> fetchAll();

    /**
     * Возвращяет все параметры в виде {@link ConfigurationParamModel}
     */
    ConfigurationParamModel fetchAllAsModel();

    /**
     * Возвращает список конфигурационных параметров определенных групп
     *
     * @param group группа параметров {@link ConfigurationParamGroup}
     * @return список параметров определенной группы
     */
    List<Configuration> fetchAllByGroup(final ConfigurationParamGroup group);

    /**
     * Возвращяет страницу конфигурационных параметров
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link Configuration}
     */
    PagingResult<Configuration> fetchAllByGroupAndPaging(ConfigurationParamGroup group, PagingParams pagingParams);

    /**
     * Возвращяет все параметры определенной группы в виде {@link ConfigurationParamModel}
     *
     * @param group группа параметров {@link ConfigurationParamGroup}
     * @return все параметры определенной группы в виде {@link ConfigurationParamModel}
     */
    ConfigurationParamModel fetchAllAsModelByGroup(ConfigurationParamGroup group);

    /**
     * Возвращяет все параметры по подразделению в виде {@link ConfigurationParamModel}
     *
     * @param departmentId идентификатор подразделения
     * @return все параметры по подразделению в виде {@link ConfigurationParamModel}
     */
    ConfigurationParamModel fetchAllByDepartment(Integer departmentId);

    /**
     * Сохраняет значения параметров в БД. Если параметр в БД отсутствует, то он создается.
     * Если в модели нет какого-либо параметра, но он есть в БД, то параметр удаляется из БД.
     */
    void save(ConfigurationParamModel model);

    /**
     * Обновляет параметр в БД у конкретных параметров
     */
    void update(Map<ConfigurationParam, String> configurationParamMap, long departmentId);

    /**
     * Обновляет значение конфигурационного параметра
     */
    void update(Configuration config);

    /**
     * Обновляет значения конфигурационных параметров
     */
    void update(List<Configuration> configurations);

    /**
     * Получение конфигураций параметров "Общие параметры"
     *
     * @param pagingParams параметры пагинации
     * @return список {@link Configuration} или пустой список
     */
    PagingResult<Configuration> fetchAllCommonParam(PagingParams pagingParams);

    /**
     * Создание нового значения для конфигураций параметров "Общие параметры"
     *
     * @param param создаваемый параметр
     * @param value значение создаваемого параметра
     */
    void createCommonParam(ConfigurationParam param, String value);

    /**
     * Удаление значенией параметров конфигураций
     *
     * @param params список удаляемых параметров
     */
    void remove(List<ConfigurationParam> params);

    /**
     * Обновление записи конфигурационного параметра "Общие параметры"
     *
     * @param commonParam обновляемый параметр
     * @param value
     */
    void updateCommonParam(ConfigurationParam commonParam, String value);

    /**
     * Обновление записи конфигурационного параметра "Параметры асинхронных задач"
     *
     * @param asyncParam обновляемый параметр
     */
    void updateAsyncParam(AsyncTaskTypeData asyncParam);
}
