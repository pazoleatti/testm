package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;
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
     * @param id    идентификатор подразделения
     * @return значение справочника
     */
    RefBookDepartment fetchDepartmentById(Integer id);

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids список идентификаторов
     * @return список значений справочника
     */
    List<RefBookDepartment> fetchDepartments(Collection<Integer> ids);

    /**
     * Получение действующих значений справочника по идентификаторам
     *
     * @param ids список идентификаторов
     * @return список значений справочника
     */
    List<RefBookDepartment> fetchActiveDepartments(Collection<Integer> ids);

    /**
     * Получение значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param ids          список идентификаторов
     * @param name         параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams параметры пейджинга
     * @return страница списка значений справочника
     */
    PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, PagingParams pagingParams);

    /**
     * Получение действующих значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param ids          список идентификаторов
     * @param name         параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams параметры пейджинга
     * @return страница списка значений справочника
     */
    PagingResult<RefBookDepartment> fetchActiveDepartments(Collection<Integer> ids, String name, PagingParams pagingParams);

    /**
     * Возвращяет список активных подразделений по типу
     *
     * @param type тип подразделений
     * @return список подразделений
     */
    List<RefBookDepartment> fetchAllActiveByType(DepartmentType type);

    /**
     * Возвращяет полное наименование (путь) подразделения, от корневого (банк, ид=0) до самого подразделения,
     * напрмер, "Банк/Байкальский банк"
     */
    String fetchFullName(Integer departmentId);
}
