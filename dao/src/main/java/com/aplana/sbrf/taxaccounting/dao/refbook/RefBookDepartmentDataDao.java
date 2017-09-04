package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookDepartmentFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

import java.util.Collection;
import java.util.List;

/**
 * Дао для работы со справочником Подразделения
 */
public interface RefBookDepartmentDataDao {
    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids Список идентификаторов
     * @return Список значений справочника
     */
    List<RefBookDepartment> fetchDepartments(Collection<Integer> ids);

    /**
     * Получение значений справочника по идентификаторам с фильтрацией и пейджингом
     *
     * @param ids          Список идентификаторов
     * @param filter       Фильтр
     * @param pagingParams Параметры пейджинга
     * @return Список значений справочника
     */
    PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, RefBookDepartmentFilter filter, PagingParams pagingParams);
}
