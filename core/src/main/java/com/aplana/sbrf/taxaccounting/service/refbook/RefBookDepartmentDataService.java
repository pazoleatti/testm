package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
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
     * Получение доступных значений справочника с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param user         Пользователь
     * @param name         Наименование подразделения
     * @param pagingParams Параметры пейджинга
     * @return Список значений справочника
     */
    PagingResult<RefBookDepartment> fetchAvailableDepartments(TAUser user, String name, PagingParams pagingParams);

    /**
     * Получение доступных значений справочника, для которых открыт заданный период, с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param user           Пользователь
     * @param name           Наименование подразделения
     * @param reportPeriodId ID отчетного периода, который должен быть открыт
     * @param pagingParams   Параметры пейджинга
     * @return Список значений справочника
     */
    PagingResult<RefBookDepartment> fetchDepartmentsWithOpenPeriod(TAUser user, String name, Integer reportPeriodId, PagingParams pagingParams);
}
