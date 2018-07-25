package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.Collection;
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
     * Возвращает общий параметр конфигурации по перечислению {@link ConfigurationParam}
     *
     * @param param перечисление {@link ConfigurationParam}
     * @return параметр {@link Configuration} или null, если не найден
     */
    Configuration fetchByEnum(ConfigurationParam param);

    /**
     * Возвращает общий параметр конфигурации по перечислению {@link ConfigurationParam}
     *
     * @param codes коллекция кодов запрашиваемых параметров
     * @return список параметров {@link Configuration} или null, если не найден
     */
    List<Configuration> fetchAllByCodes(Collection<String> codes);

    /**
     * Возвращает все параметры в виде модели {@link ConfigurationParamModel}
     */
    @Deprecated
    ConfigurationParamModel fetchAllAsModel();

    /**
     * Возвращает страницу конфигурационных параметров по группе из перечисления {@link ConfigurationParamGroup}
     *
     * @param group        группа параметров
     * @param pagingParams параметры пагинации
     * @return страница {@link Configuration} или пустая страница
     */
    PagingResult<Configuration> fetchAllByGroupAndPaging(ConfigurationParamGroup group, PagingParams pagingParams);

    /**
     * Возвращает все параметры определенной группы в виде {@link ConfigurationParamModel}
     *
     * @param group группа параметров {@link ConfigurationParamGroup}
     * @return все параметры определенной группы в виде {@link ConfigurationParamModel}
     */
    ConfigurationParamModel fetchAllAsModelByGroup(ConfigurationParamGroup group);

    /**
     * Возвращает все параметры по подразделению в виде {@link ConfigurationParamModel}
     *
     * @param departmentId идентификатор подразделения
     * @return все параметры по подразделению в виде {@link ConfigurationParamModel}
     */
    ConfigurationParamModel fetchAllByDepartment(Integer departmentId);

    /**
     * Сохраняет значения параметров в БД. Если параметр в БД отсутствует, то он создается.
     * Если в модели нет какого-либо параметра, но он есть в БД, то параметр удаляется из БД.
     *
     * @param model модель-хранилище мапы конфигурационных параметров
     */
    @Deprecated
    void save(ConfigurationParamModel model);

    /**
     * Обновляет параметр в БД у конкретных параметров
     *
     * @param configurationParamMap мапа обновляемых парметров (параметр - значение)
     * @param departmentId          идентификатор подразделения
     */
    @Deprecated
    void update(Map<ConfigurationParam, String> configurationParamMap, long departmentId);

    /**
     * Обновляет значение конфигурационного параметра
     *
     * @param config обновляемый параметр
     */
    void update(Configuration config);

    /**
     * Обновляет значения конфигурационных параметров
     *
     * @param configurations список обновляемых параметров
     */
    void update(List<Configuration> configurations);

    /**
     * Создание общего параметра конфигурации
     *
     * @param configuration создаваемая конфигурация
     */
    void createCommonParam(Configuration configuration);

    /**
     * Удаление общих параметров конфигураций
     *
     * @param params список удаляемых параметров
     */
    void removeCommonParam(List<ConfigurationParam> params);

    /**
     * Обновление записи конфигурационного параметра типов асинхронных задач
     *
     * @param asyncParam обновляемый параметр
     */
    void updateAsyncParam(AsyncTaskTypeData asyncParam);

    /**
     * Обновление записи конфигурационного параметра електронной почты
     *
     * @param emailParam обновляемый параметр
     */
    void updateEmailParam(Configuration emailParam);

    /**
     * Получение конфигурационных параметров электронной почты
     *
     * @param pagingParams параметры пагинации
     * @return страница {@link PagingResult} с данными {@link Configuration}
     */
    PagingResult<Configuration> fetchEmailParams(PagingParams pagingParams);

    /**
     * Получение конфигурационных параметров электронной почты, необходимые для авторизации почтового клиента
     *
     * @return мапа <название, значение> с данными {@link Configuration}
     */
    List<Configuration> fetchAuthEmailParams();
}
