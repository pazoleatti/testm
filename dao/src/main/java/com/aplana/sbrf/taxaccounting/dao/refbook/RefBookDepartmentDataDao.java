package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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
     * Получение значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param ids          Список идентификаторов
     * @param name         Наименование подразделения
     * @param pagingParams Параметры пейджинга
     * @return Список значений справочника
     */
    PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, PagingParams pagingParams);
}
