package com.aplana.sbrf.taxaccounting.service;


import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс сервиса для работы с {@link DepartmentReportPeriod}
 */
public interface DepartmentReportPeriodService {

    /**
     * Возвращает объект {@link DepartmentReportPeriod} по идентификатору
     *
     * @param id идентификаор
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod fetchOne(int id);

    /**
     * Получение списка отчетных периодов для подразделений по указанному фильтру
     *
     * @param departmentReportPeriodFilter фильтр
     * @return список {@link DepartmentReportPeriod} или пустой список
     */
    List<DepartmentReportPeriod> fetchAllByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Получение списка идентификаторов отчетных периодов для подразделений по указанному фильтру
     *
     * @param departmentReportPeriodFilter фильтр
     * @return список идентификаторов или пустой список
     */
    List<Integer> fetchAllIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Создание нового отчетного периода для подразделения
     *
     * @param departmentReportPeriod сохраняемый объект
     */
    void create(DepartmentReportPeriod departmentReportPeriod);

    /**
     * Создание нового отчетного периода для подразделения
     *
     * @param departmentReportPeriod сохраняемый объект
     * @param departmentIds          список идентификаторв подразделений, для которых необходимо создать отчетный период
     */
    void create(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds);

    /**
     * Открытие/закрытие отчетного периода подразделения
     *
     * @param id     идентификатор отчетного периода подразделения, статус которого необходимо обновить
     * @param active статус
     */
    void updateActive(int id, boolean active);

    /**
     * Открытие/закрытие отчетного периода подразделения
     *
     * @param ids            список идентификаторов отчетных периодов подразделения, для которых необходимо обновить статус
     * @param reportPeriodId идентификатор отчетного периода
     * @param active         статус (открыт/закрыт)
     */
    void updateActive(List<Integer> ids, Integer reportPeriodId, boolean active);

    /**
     * Удаление отчетных периодов подразделений
     *
     * @param ids список иденификаторов
     */
    void delete(List<Integer> ids);

    /**
     * Проверяет существование периода для подразделения
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @return признак существования отчетного периода пожразделения
     */
    boolean checkExistForDepartment(int departmentId, int reportPeriodId);

    /**
     * Получение последнего отчетного периода подразделения
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod fetchLast(int departmentId, int reportPeriodId);

    /**
     * Получение первого некорректирующего отчетного периода подразделения по идентификатору подразделения
     * и идентификатору отчетного периода
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod fetchFirst(int departmentId, int reportPeriodId);

    /**
     * Проверяет существует ли корректирующий период, атрибут "Период сдачи корректировки" которого содержит большее значение
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @param correctionDate дата выбранного периода
     * @return true, если существует
     */
    boolean checkExistLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate);

    /**
     * Возвращает отчетные периоды подразделений с фильтрацией и пагинацией
     *
     * @param departmentReportPeriodFilter фильтр отчетных периодов подразделений
     * @return список {@link DepartmentReportPeriodJournalItem} или пустой список
     */
    List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Проверка периода на декларации, которые не в статусе "Принят"
     *
     * @param id параметры проверяемого периода
     * @return uuid идентификатор логгера
     */
    String checkHasNotAccepted(Integer id);

    /**
     * Проверяет период на наличие деклараций, которые находятся на редактировании
     *
     * @param id идентификатор проверяемого периода
     * @return uuid  идентификатор логера
     */
    String checkHasBlockedDeclaration(Integer id);
}
