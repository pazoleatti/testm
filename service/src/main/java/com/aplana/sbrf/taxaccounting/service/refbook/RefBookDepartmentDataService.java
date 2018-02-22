package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Сервис для работы со справочником Подразделения
 */
public interface RefBookDepartmentDataService {
    /**
     * Получение подразделения пользователя
     *
     * @param user Пользователь
     * @return Подразделение
     */
    RefBookDepartment fetchUserDepartment(TAUser user);

    /**
     * Получение доступных (согласно правам доступа пользователя)  значений справочника
     *
     * @param user Пользователь
     * @return Список значений справочника
     */
    List<RefBookDepartment> fetchAllAvailableDepartments(TAUser user);

    /**
     * Получение доступных (согласно правам доступа пользователя) значений справочника с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param user         Пользователь
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    PagingResult<RefBookDepartment> fetchAvailableDepartments(TAUser user, String name, PagingParams pagingParams);

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
     * Получение действующих доступных (согласно правам доступа пользователя) значений справочника, для которых открыт заданный период,
     * с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param user           Пользователь
     * @param name           Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                       наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param reportPeriodId ID отчетного периода, который должен быть открыт
     * @param pagingParams   Параметры пейджинга
     * @return Страница списка значений справочника
     */
    PagingResult<RefBookDepartment> fetchActiveDepartmentsWithOpenPeriod(TAUser user, String name, Integer reportPeriodId, PagingParams pagingParams);

    /**
     * Получение действующих доступных (согласно правам доступа пользователя) значений ТБ справочника подразделений.
     *
     * @param user Пользователь
     * @return Список значений справочника
     */
    List<RefBookDepartment> fetchActiveAvailableTB(TAUser user);

    /**
     * Получение мапы, в которой ключ - идентификатор типа налоговой формы, значение - список идентификаторов подразделений,
     * для которых подразделение пользователя назначено исполнителем налоговой формы, содержащейся в ключе
     *
     * @param currentUser Информация о пользовател
     * @return мапа {@link DeclarationType#getId()} -> {@link Set<Integer>} или пустая мапа
     */
    Map<Integer, Set<Integer>> fetchAllAvailableDepartmentsForEachDeclarationType(TAUser currentUser);
}
