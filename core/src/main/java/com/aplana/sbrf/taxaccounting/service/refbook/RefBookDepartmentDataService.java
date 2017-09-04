package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookDepartmentFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

import java.util.List;

/**
 * Сервис для работы со справочником Подразделения
 */
public interface RefBookDepartmentDataService {
    /**
     * Получение всех доступных значений справочника
     *
     * @param user Пользователь
     * @return Список значений справочника
     */
    List<RefBookDepartment> fetchAllAvailableDepartments(TAUser user);

    /**
     * Получение доступных значений справочника с фильтрацией и пейджингом
     *
     * @param user         Пользователь
     * @param filter       Фильтр
     * @param pagingParams Параметры пейджинга
     * @return Список значений справочника
     */
    PagingResult<RefBookDepartment> fetchAvailableDepartments(TAUser user, RefBookDepartmentFilter filter, PagingParams pagingParams);
}
