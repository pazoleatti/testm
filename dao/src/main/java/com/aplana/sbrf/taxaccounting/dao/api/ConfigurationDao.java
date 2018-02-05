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
     * Читает из БД значения всех параметров.
     */
    ConfigurationParamModel getAll();

    ConfigurationParamModel getConfigByGroup(ConfigurationParamGroup group);

    ConfigurationParamModel getByDepartment(Integer departmentId);

    /**
     * Сохраняет значения параметров в БД. Если параметр в БД отсутствует, то он создается.
     * Если в модели нет какого-либо параметра, но он есть в БД, то параметр удаляется из БД.
     */
    void save(ConfigurationParamModel model);

    /**
     * Создаёт параметр в БД.
     */
    void create(Configuration config);

    /**
     * Обновляет параметр в БД у конкретных параметров
     */
    void update(Map<ConfigurationParam, String> configurationParamMap, long departmentId);

    /**
     * Возвращает список конфигурационных параметров определенных групп
     */
    List<Configuration> getListConfigByGroup(final ConfigurationParamGroup group);

    /**
     * Обновление конфигурационного параметра
     */
    void update(Configuration config);

    /**
     * Список всех конфигурационных параметров
     */
    List<Configuration> getAllConfiguration();

    /**
     * Установка общих параметров по умолчанию
     */
    void setCommonParamsDefault(List<Configuration> listdefaulConfig);

    /**
     * Получение конфигураций параметров "Общие параметры"
     *
     * @param pagingParams параметры пагинации
     * @return список {@link CommonConfigurationParam} или пустой список
     */
    PagingResult<CommonConfigurationParam> fetchAllCommonParam(PagingParams pagingParams);
}
