package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;

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
	int save(DepartmentReportPeriod departmentReportPeriod);

    void save(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(int id, boolean active, boolean isBalance);

    /**
     * Открытие/закрытие отчетного периода подразделения (batch)
     */
    void updateActive(final List<Integer> ids, final Integer report_period_id, final boolean active, final boolean isBalance);

    /**
     * Открытие/закрытие отчетного периода подразделения (batch)
     */
    void updateActive(List<Integer> ids, final Integer report_period_id, boolean active);

    /**
     * Изменить дату корректировки
     */
    void updateCorrectionDate(int id, Date correctionDate);

    /**
     * Изменить признак периода ввода остатков
     */
    void updateBalance(int id, boolean isBalance);

    /**
     * Изменить признак периода ввода остатков(батчем)
     * @param ids список периодов
     */
    void updateBalance(List<Integer> ids, boolean isBalance);

    /**
     * Удаление отчетного периода подразделения
     */
    void delete(int id);

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
}
