package com.aplana.sbrf.taxaccounting.service;


import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;

import java.util.Date;
import java.util.List;

public interface DepartmentReportPeriodService {
    /**
     * Отчетный период подразделения
     */
    DepartmentReportPeriod get(int id);

    /**
     * Отчетные периоды подразделений (корректирующий) по параметрам фильтрации (null допустим)
     */
    List<DepartmentReportPeriod> getListByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Сохранение отчетноего периода подразделения
     */
    int save(DepartmentReportPeriod departmentReportPeriod);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(int id, boolean active);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(List<DepartmentReportPeriod> drps, boolean active, List<LogEntry> logs);

    /**
     * Изменить дату корректировки
     */
    void updateCorrectionDate(int id, Date correctionDate);

    /**
     * Изменить признак периода ввода остатков
     */
    void updateBalance(int id, boolean isBalance);

    /**
     * Удаление отчетного периода подразделения
     */
    void delete(int id);

    /**
     * Проверяет существование периода для подразделения
     */
    boolean existForDepartment(int departmentId, int reportPeriodId);

    /**
     * Номер корректирующего периода
     */
    Integer getCorrectionNumber(int id);
}
