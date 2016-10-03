package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentChange;
import com.aplana.sbrf.taxaccounting.model.ReportType;

import java.util.List;

/**
 * Сервис работы с изменениями подразделений
 */
public interface DepartmentChangeService {

    List<DepartmentChange> getAllChanges();

    void clear();

    void addChange(DepartmentChange departmentChange);

    boolean checkDepartment(int depId, Integer depParentId);

    /**
     * Генерация ключа блокировки для задачи c типом reportType
     * @param reportType тип задачи
     * @return
     */
    String generateTaskKey(ReportType reportType);
}
