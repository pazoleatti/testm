package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.DepartmentFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

import java.util.Collection;
import java.util.List;

/**
 * Дао для работы со справочником Подразделения
 */
public interface RefBookDepartmentDao {
    /**
     * Получение значения справочника по идентификатору
     *
     * @param id идентификатор подразделения
     * @return значение справочника
     */
    RefBookDepartment fetchDepartmentById(Integer id);

    /**
     * Возвращяет родительский ТБ
     *
     * @param id ид подразделение
     * @return подразделение типа "ТерБанк"
     */
    RefBookDepartment findParentTBById(int id);

    /**
     * Возвращяет список подразделений по наименованию (через оператор like), включая родительские подразделения у найденных
     *
     * @param name        наименование подразделения
     * @param exactSearch признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return список подразделений
     */
    List<RefBookDepartment> findAllByName(String name, boolean exactSearch);

    /**
     * То же что {@link #findAllByName(String, boolean)}, но в виде дерева
     *
     * @param name        наименование подразделения
     * @param exactSearch признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return список подразделений
     */
    List<RefBookDepartment> findAllByNameAsTree(String name, boolean exactSearch);

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids список идентификаторов
     * @return список значений справочника
     */
    List<RefBookDepartment> findAllActiveByIds(Collection<Integer> ids);

    /**
     * Получение значений справочника с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param name         параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams параметры пейджинга
     * @return страница списка значений справочника
     */
    PagingResult<RefBookDepartment> findDepartments(String name, PagingParams pagingParams);

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
     * Возвращяет страницу из списка действующих подразделений с заданной фильтрацией и пейджингом
     *
     * @param filter       фильтр
     * @param pagingParams параметры пейджинга
     * @return страница списка значений справочника
     */
    PagingResult<RefBookDepartment> findAllByFilter(DepartmentFilter filter, PagingParams pagingParams);

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

    /**
     * Найти активные подразделения исключая присутствующие
     * @param type              тип подразделения
     * @param presentedTbidList идентификаторы подразделений которые нужно исключить из выборки
     * @return  список подразделений
     */
    List<RefBookDepartment> findActiveByTypeExcludingPresented(DepartmentType type, List<Integer> presentedTbidList);
}
