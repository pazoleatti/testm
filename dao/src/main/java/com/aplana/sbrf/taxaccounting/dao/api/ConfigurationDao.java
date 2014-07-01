package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;

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

    /**
     * Сохраняет значения параметров в БД. Если параметр в БД отсутствует, то он создается.
     * Если в модели нет какого-либо параметра, но он есть в БД, то параметр удаляется из БД.
     */
    void save(ConfigurationParamModel model);
}
