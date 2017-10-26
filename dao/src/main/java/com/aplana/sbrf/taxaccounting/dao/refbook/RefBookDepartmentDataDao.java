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
     * Получение значения справочника по идентификатору
     *
     * @param id Идентификатор подразделения
     * @return Значение справочника
     */
    RefBookDepartment fetchDepartmentById(Integer id);

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
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части наименования
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, PagingParams pagingParams);
}
