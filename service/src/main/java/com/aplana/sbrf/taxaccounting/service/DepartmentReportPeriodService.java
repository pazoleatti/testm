package com.aplana.sbrf.taxaccounting.service;


import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import org.joda.time.LocalDateTime;

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

    List<Long> getListIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Сохранение отчетноего периода подразделения
     */
    DepartmentReportPeriod save(DepartmentReportPeriod departmentReportPeriod);

    void save(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(long id, boolean active);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(List<Long> ids, Integer report_period_id, boolean active);

    /**
     * Изменить дату корректировки
     */
    void updateCorrectionDate(Long id, LocalDateTime correctionDate);

    /**
     * Удаление отчетного периода подразделения
     * @param id
     */
    void delete(long id);

    void delete(List<Long> ids);

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
    boolean existLargeCorrection(int departmentId, int reportPeriodId, LocalDateTime correctionDate);


    /**
     * Возвращает отчетные периоды подразделений с фильтрацией и пагинацией
     * @param departmentReportPeriodFilter - фильтр отчетных периодов подразделений
     * @param pagingParams - параметры пагинации
     * @return отчетные периоды подразделений
     */
    PagingResult<DepartmentReportPeriodJournalItem> findAll(DepartmentReportPeriodFilter departmentReportPeriodFilter, PagingParams pagingParams);

    /**
     * Проверка периода на декларации, которые не в статусе "Принят"
     * @param id - параметры проверяемого периода
     * @return uuid - идентификатор логгера
     */
    String checkHasNotAccepted(Long id);

    DepartmentReportPeriod findOne(Long departmentRPId);
}
