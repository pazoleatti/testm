package com.aplana.sbrf.taxaccounting.service;


import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
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
    DepartmentReportPeriod save(DepartmentReportPeriod departmentReportPeriod);

    void save(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(int id, boolean active);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(List<Integer> ids, Integer report_period_id, boolean active);

    /**
     * Изменить дату корректировки
     */
    void updateCorrectionDate(int id, Date correctionDate);

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


    /**
     * Возвращает отчетные периоды подразделений с фильтрацией и пагинацией
     * @param departmentReportPeriodFilter - фильтр отчетных периодов подразделений
     * @return отчетные периоды подразделений
     */
    List<DepartmentReportPeriodJournalItem> findAll(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Проверка периода на декларации, которые не в статусе "Принят"
     * @param id - параметры проверяемого периода
     * @return uuid - идентификатор логгера
     */
    String checkHasNotAccepted(Integer id);

    /**
     * Проверяет период на наличие деклараций, которые находятся на редактировании
     * @param id - идентификатор проверяемого периода
     * @return uuid - идентификатор логера
     */
    String checkHasBlockedDeclaration(Integer id);
}
