package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Интерфейс DAO для работы с {@link DepartmentReportPeriod отчётными периодами для подразделений}
 */
public interface DepartmentReportPeriodDao {

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
     * Открытие/закрытие отчетного периода подразделения
     * @param id - идентификатор отчетного периода подразделения
     * @param active - статус (открыт/закрыт)
     */
    void updateActive(int id, boolean active);

    /**
     * Открытие/закрытие отчетного периода подразделения (batch)
     * @param ids - список идентификаторов отчетных периодов подразделения, для которых необходимо обновить статус
     * @param report_period_id - идентификатор отчетного периода
     * @param active - статус (открыт/закрыт)
     */
    void updateActive(List<Integer> ids, final Integer report_period_id, boolean active);


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
     * Предпоследний отчетный период подразделения для комбинации отчетный период-подразделение.
     * Если предпоследний отчетный период не является корректировочным возвращается null
     * @param departmentId - идентификатор подразделения
     * @param reportPeriodId - идентификатор отчетного периода
     * @return отчтетный период подразделения
     */
    DepartmentReportPeriod fetchPrevLast(int departmentId, int reportPeriodId);

    /**
     * Возвращает номер корректирующего периода
     * @param id идентификатор корректирующего периода
     */
    Integer fetchCorrectionNumber(int id);

    /**
     * Возвращает признак наличия более позднего периода корректировки
     * @param departmentId - идентификатор подразделения
     * @param reportPeriodId - идентификатор отчетного периода
     * @param correctionDate - период сдачи корректировки
     */
    boolean checkExistLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate);

    /**
     * Возвращает списков дат корректирующих периодов по отчетным периодам
     * @param reportPeriodIdList - список идентификаторов отчетных периодов
     * @return мапу <reportPeriodId, List<correctionDate>>
     */
    Map<Integer, List<Date>> fetchCorrectionDateListByReportPeriod(Collection<Integer> reportPeriodIdList);

    /**
     * Найти id отчетных периодов подразделений для определенного типа подразделения и активного отчетного периода
     * @param departmentTypeCode
     * @param departmentReportPeriodId
     * @return
     */
    List<Integer> fetchIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId);

    /**
     * Возвращает отчетные периоды подразделений с фильтрацией и пагинацией
     * @param departmentReportPeriodFilter - фильтр отчетных периодов подразделений
     * @return отчетные периоды подразделений
     */
    List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);


}
