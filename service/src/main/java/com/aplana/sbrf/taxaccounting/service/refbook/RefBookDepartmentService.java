package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.filter.DepartmentFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.result.RefBookDepartmentDTO;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

/**
 * Сервис для работы со справочником Подразделения
 */
@ScriptExposed
public interface RefBookDepartmentService {

    /**
     * Получение значения справочника по идентификатору
     *
     * @param id идентификатор подразделения
     * @return значение справочника
     */
    RefBookDepartment fetch(Integer id);

    /**
     * Возвращяет родительский ТБ
     *
     * @param id ид подразделения
     * @return подразедление типа "ТерБанк"
     */
    RefBookDepartment findParentTBById(int id);

    /**
     * Получение подразделения пользователя
     *
     * @param user Пользователь
     * @return Подразделение
     */
    RefBookDepartment fetchUserDepartment(TAUser user);

    /**
     * Получение значений справочника подразделений с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    PagingResult<RefBookDepartment> findDepartments(String name, PagingParams pagingParams);

    /**
     * Возвращяет список ТБ с установленными дочерними подразделениями
     *
     * @param searchPattern Строка с запросом поиска по справочнику
     * @param exactSearch   Признак того, что результат поиска должен быть с полным соответствием поисковой строке
     * @return список подразделений
     */
    List<RefBookDepartmentDTO> findAllTBWithChildren(String searchPattern, boolean exactSearch);

    /**
     * Получение доступных (согласно правам доступа пользователя) для бизнес-администрирования подразделений с фильтрацией по наименованию и пейджингом
     *
     * @param user         Пользователь
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    PagingResult<RefBookDepartment> fetchAvailableBADepartments(TAUser user, String name, PagingParams pagingParams);

    /**
     * Получение подразделений, доступных (согласно правам доступа пользователя) для назначения исполнителями, с фильтрацией по наименованию и пейджингом
     *
     * @param user         Пользователь
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    PagingResult<RefBookDepartment> fetchAvailableDestinationDepartments(TAUser user, String name, PagingParams pagingParams);

    /**
     * Возвращяет страницу из списка действующих подразделений, доступных пользователю, с заданной фильтрацией и пейджингом
     *
     * @param filter       фильтр
     * @param pagingParams Параметры пейджинга
     * @param user         Пользователь
     * @return страница списка подразделений
     */
    PagingResult<RefBookDepartment> findAllByFilter(DepartmentFilter filter, PagingParams pagingParams, TAUser user);

    /**
     * Получение действующих доступных (согласно правам доступа пользователя) значений ТБ справочника подразделений.
     *
     * @param user пользователь, для которого будет проверяться доступность подразделения
     * @return список подразделений
     */
    List<RefBookDepartment> fetchActiveAvailableTB(TAUser user);

    /**
     *  Получение ТБ справочника подразделений, исключая присутствующие
     * @param presentedTbidList список идентификаторов присутствующих тербанков
     * @return  список подразделений
     */
    List<RefBookDepartment> findTbExcludingPresented(List<Integer> presentedTbidList);
}
