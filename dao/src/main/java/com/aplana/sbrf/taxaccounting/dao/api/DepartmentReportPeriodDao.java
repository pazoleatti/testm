package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Отчетные периоды подразделений DEPARTMENT_REPORT_PERIOD
 */
public interface DepartmentReportPeriodDao {

    /**
     * Отчетный период подразделения
     */
    DepartmentReportPeriod get(int id);

    /**
     * Отчетные периоды подразделений по параметрам фильтрации (null допустим)
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
     * Открытие/закрытие отчетного периода подразделения (batch)
     */
    void updateActive(List<Integer> ids, final Integer report_period_id, boolean active);

    /**
     * Изменить дату корректировки
     */
    void updateCorrectionDate(int id, Date correctionDate);

    /**
     * Удаление отчетного периода подразделения
     */
    void delete(Integer id);

    /**
     * Удаление отчетных периода подразделения
     */
    void delete(List<Integer> ids);

	/**
	 * Проверяет существование периода для подразделения
	 */
	boolean existForDepartment(int departmentId, int reportPeriodId);

    /**
     * Последний отчетный период подразделения для комбинации отчетный период-подразделение
     */
    DepartmentReportPeriod getLast(int departmentId, int reportPeriodId);

    /**
     * Обычный отчетный период подразделения для комбинации отчетный период-подразделение (первый и без корректировки)
     */
    DepartmentReportPeriod getFirst(int departmentId, int reportPeriodId);

    /**
     * Предпоследний отчетный период подразделения для комбинации отчетный период-подразделение.
     * Если предпоследний отчетный период не является корректировочным возвращается null
     */
    DepartmentReportPeriod getPrevLast(int departmentId, int reportPeriodId);

    /**
     * Номер корректирующего периода
     */
    Integer getCorrectionNumber(int id);

    boolean existLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate);

    /**
     * Получение списков дат корректирующих периодов по отчетным периодам
     */
    Map<Integer, List<Date>> getCorrectionDateListByReportPeriod(Collection<Integer> reportPeriodIdList);

    /**
     * Список закрытых отчетных периодов подразделений, в которых есть экремляры НФ узазанного шаблона
     */
    List<DepartmentReportPeriod> getClosedForFormTemplate(int formTemplateId);


    /**
     * Найти id отчетных периодов подразделений для определенного типа подразделения и активного отчетного периода
     * @param departmentTypeCode
     * @param departmentReportPeriodId
     * @return
     */
    List<Integer> getIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId);

    /**
     * Возвращает отчетные периоды подразделений с фильтрацией и пагинацией
     * @param departmentReportPeriodFilter - фильтр отчетных периодов подразделений
     * @return отчетные периоды подразделений
     */
    List<DepartmentReportPeriodJournalItem> findAll(DepartmentReportPeriodFilter departmentReportPeriodFilter);


}
