package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;

import java.util.List;

/**
 * Сервис для работы со справочником Подразделения
 */
public interface RefBookDepartmentDataService {
    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    List<RefBookDepartment> fetchDepartments();
}
