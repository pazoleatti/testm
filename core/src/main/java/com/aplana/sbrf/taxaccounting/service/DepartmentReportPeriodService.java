package com.aplana.sbrf.taxaccounting.service;


import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
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

    List<Integer> getListIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Сохранение отчетноего периода подразделения
     */
    int save(DepartmentReportPeriod departmentReportPeriod);

    void save(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(int id, boolean active, boolean isBalance);

    void updateActive(List<Integer> ids, boolean active, boolean isBalance);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(List<Integer> ids, boolean active);

    /**
     * Изменить дату корректировки
     */
    void updateCorrectionDate(int id, Date correctionDate);

    /**
     * Изменить признак периода ввода остатков
     */
    void updateBalance(int id, boolean isBalance);

    void updateBalance(List<Integer> id, boolean isBalance);

    /**
     * Удаление отчетного периода подразделения
     */
    void delete(int id);

    void delete(List<Integer> ids);

    /**
     * Проверяет существование периода для подразделения
     */
    boolean existForDepartment(int departmentId, int reportPeriodId);

    /**
     * Номер корректирующего периода
     */
    Integer getCorrectionNumber(int id);

    /**
     * Последний отчетный период подразделения для комбинации отчетный период-подразделение
     */
    DepartmentReportPeriod getLast(int departmentId, int reportPeriodId);

    /**
     * Обычный отчетный период подразделения для комбинации отчетный период-подразделение (первый и без корректировки)
     */
    DepartmentReportPeriod getFirst(int departmentId, int reportPeriodId);

    /**
     * Проверяет существует ли корректирующий период, атрибут "Период сдачи корректировки" которого содержит большее значение
     * @param departmentId пара определяющая период
     * @param reportPeriodId  пара определяющая период
     * @param correctionDate дата выбранного периода
     * @return true - если существует
     */
    boolean existLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate);
}
