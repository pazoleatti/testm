package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;

import java.util.List;

/**
 * Сервис для работы с настройками подразделениями
 */
public interface DepartmentConfigService {
    /**
     * Получает настройки подразделений для отображения на представлении
     *
     * @param action       объект содержащих данные используемые для фильтрации
     * @param pagingParams параметры пагиинации
     * @return список объектов содержащих данные о настройках подразделений
     */
    PagingResult<DepartmentConfig> fetchDepartmentConfigs(DepartmentConfigsFilter action, PagingParams pagingParams);

    /**
     * Возвращяет кол-во записей настроек подразделений по фильтру
     *
     * @param filter фильтр
     * @return кол-во записей
     */
    int fetchCount(DepartmentConfigsFilter filter);

    /**
     * Создаёт запись настройки подразделений
     *
     * @param departmentConfig данные записи настроек подразделений
     */
    ActionResult create(DepartmentConfig departmentConfig, TAUserInfo user);

    /**
     * Изменяет запись настройки подразделений
     *
     * @param departmentConfig данные записи настроек подразделений
     */
    ActionResult update(DepartmentConfig departmentConfig, TAUserInfo user);

    /**
     * Удаляет запись настройки подразделений
     *
     * @param ids список id записи настроек подразделений
     */
    ActionResult delete(List<Long> ids, TAUserInfo user);

    /**
     * Создает асинхронную задачу на формирование excel
     *
     * @param filter       фильтр
     * @param pagingParams параметры сортировки
     * @param userInfo     пользователь запустивший операцию
     * @return результат создания задачи
     */
    ActionResult createTaskToCreateExcel(DepartmentConfigsFilter filter, PagingParams pagingParams, TAUserInfo userInfo);
}
