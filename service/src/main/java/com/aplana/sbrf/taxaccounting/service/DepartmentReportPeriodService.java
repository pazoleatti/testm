package com.aplana.sbrf.taxaccounting.service;


import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс сервиса для работы с {@link DepartmentReportPeriod отчётный период для подразделения}
 */
public interface DepartmentReportPeriodService {


    /**
     * Возвращает объект {@link DepartmentReportPeriod отчётный период для подразделения} по идентификатору
     * @param id - идентификаор
     * @return объект {@link DepartmentReportPeriod}
     */
    DepartmentReportPeriod fetchOne(int id);

    /**
     * Получение списка отчетных периодов для подразделений по указанному фильтру
     * @param departmentReportPeriodFilter - фильтр
     * @return список периодов
     */
    List<DepartmentReportPeriod> fetchAllByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Получение списка идентификаторов отчетных периодов для подразделений по указанному фильтру
     * @param departmentReportPeriodFilter - фильтр
     * @return список идентификаторов
     */
    List<Integer> fetchAllIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Сохранение в БД отчетного периода для подразделения
     * @param departmentReportPeriod - сохраняемый объект
     */
    void create(DepartmentReportPeriod departmentReportPeriod);

    /**
     * Сохранение в БД отчетного периода для подразделения
     * @param departmentReportPeriod - сохраняемый объект
     * @param departmentIds - список идентификаторв подразделений, для которых необходимо создать отчетный период
     */
    void create(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(int id, boolean active);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(List<Integer> ids, Integer report_period_id, boolean active);

    /**
     * Удаление отчетного периода подразделения
     * @param id - идентификатор
     */
    void delete(Integer id);

    /**
     * Удаление отчетных периода подразделения
     * @param ids - список иденификаторов
     */
    void delete(List<Integer> ids);

    /**
     * Проверяет существование периода для подразделения
     * @param departmentId - идентификатор подразделения
     * @param reportPeriodId - идентификатор отчетного периода
     * @return признак существования отчетного периода пожразделения
     */
    boolean checkExistForDepartment(int departmentId, int reportPeriodId);

    /**
     * Возвращает последний отчетный период подразделения для комбинации отчетный период-подразделение
     * @param departmentId - идентификатор подразделения
     * @param reportPeriodId - идентификатор отчетного периода
     * @return последний отчетный период подразделения
     */
    DepartmentReportPeriod fetchLast(int departmentId, int reportPeriodId);

    /**
     * Обычный отчетный период подразделения для комбинации отчетный период-подразделение (первый и без корректировки)
     * @param departmentId - идентификатор подразделения
     * @param reportPeriodId - идентификатор отчетного периода
     * @return отчетный период подразделения
     */
    DepartmentReportPeriod fetchFirst(int departmentId, int reportPeriodId);

    /**
     * Проверяет существует ли корректирующий период, атрибут "Период сдачи корректировки" которого содержит большее значение
     * @param departmentId пара определяющая период
     * @param reportPeriodId  пара определяющая период
     * @param correctionDate дата выбранного периода
     * @return true - если существует
     */
    boolean checkExistLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate);


    /**
     * Возвращает отчетные периоды подразделений с фильтрацией и пагинацией
     * @param departmentReportPeriodFilter - фильтр отчетных периодов подразделений
     * @return отчетные периоды подразделений
     */
    List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

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
