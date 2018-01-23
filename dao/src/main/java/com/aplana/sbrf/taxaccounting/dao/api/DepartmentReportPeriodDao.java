package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;

import java.util.Date;
import java.util.List;

/**
 * Интерфейс DAO для работы с {@link DepartmentReportPeriod}
 */
public interface DepartmentReportPeriodDao {

    /**
     * Получение объекта {@link DepartmentReportPeriod} по идентификатору
     *
     * @param id идентификатор
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod fetchOne(int id);

    /**
     * Получение списка отчетных периодов для подразделений по фильтру
     *
     * @param departmentReportPeriodFilter фильтр
     * @return список {@link DepartmentReportPeriod} или пустой список
     */
    List<DepartmentReportPeriod> fetchAllByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Получение списка идентификаторов отчетных периодов для подразделений по фильтру
     *
     * @param departmentReportPeriodFilter фильтр
     * @return список идентификаторов или пустой список
     */
    List<Integer> fetchAllIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

    /**
     * Сохранение в БД отчетного периода для подразделения
     *
     * @param departmentReportPeriod сохраняемый объект {@link DepartmentReportPeriod}
     */
    void create(DepartmentReportPeriod departmentReportPeriod);

    /**
     * Открытие/закрытие отчетного периода подразделения
     *
     * @param id     идентификатор отчетного периода подразделения
     * @param active статус (открыт/закрыт)
     */
    void updateActive(int id, boolean active);

    /**
     * Открытие/закрытие отчетного периода подразделения в пакетном режиме
     *
     * @param ids            список идентификаторов отчетных периодов подразделения, для которых необходимо обновить статус
     * @param reportPeriodId идентификатор отчетного периода
     * @param active         статус (открыт/закрыт)
     */
    void updateActive(List<Integer> ids, Integer reportPeriodId, boolean active);

    /**
     * Удаление отчетных периодов подразделений в пакетном режиме
     *
     * @param ids список иденификаторов
     */
    void delete(List<Integer> ids);

    /**
     * Проверка существования периода для подразделения по идентификатору подразделения
     * и идентификатору отчетного периода
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @return признак существования отчетного периода подразделения
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
     * Получение предпоследнего отчетного периода подразделения по идентификатору подразделения
     * и идентификатору отчетного периода
     * Если предпоследний отчетный период не является корректировочным возвращается null
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod fetchPrevLast(int departmentId, int reportPeriodId);

    /**
     * Возвращает признак наличия более позднего периода корректировки
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @param correctionDate период сдачи корректировки
     * @return признак наличия более позднего периода корректировки
     */
    boolean checkExistLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate);

    /**
     * Получение идентификаторов некорректирующих отчетных периодов подразделений по типу подразделения и
     * активному отчетному периоду
     *
     * @param departmentTypeCode       {@link DepartmentType} тип подразделения
     * @param departmentReportPeriodId идентификатор отчетного периода подразделения, по {@link ReportPeriod} которого
     *                                 производится поиск
     * @return список идентификаторов или пустой список
     */
    List<Integer> fetchIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId);

    /**
     * Получение отчетных периодов подразделений с фильтрацией
     *
     * @param departmentReportPeriodFilter фильтр отчетных периодов подразделений
     * @return список отчетных периодов подразделений или пустой список
     */
    List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);
}
