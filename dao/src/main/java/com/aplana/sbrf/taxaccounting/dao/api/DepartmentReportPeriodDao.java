package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import org.joda.time.LocalDateTime;
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

    List<Long> getListIdsByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter);

	/**
	 * Сохранение отчетноего периода подразделения
	 */
	DepartmentReportPeriod save(DepartmentReportPeriod departmentReportPeriod);

    void save(DepartmentReportPeriod departmentReportPeriod, List<Integer> departmentIds);

    /**
     * Открытие/закрытие отчетного периода подразделения
     */
    void updateActive(long lo, boolean active);

    /**
     * Открытие/закрытие отчетного периода подразделения (batch)
     */
    void updateActive(List<Long> ids, final Integer report_period_id, boolean active);

    /**
     * Изменить дату корректировки
     */
    void updateCorrectionDate(Long id, LocalDateTime correctionDate);

    /**
     * Удаление отчетного периода подразделения
     * @param id
     */
    void delete(long id);

    /**
     * Удаление отчетных периода подразделения
     * @param ids
     */
    void delete(List<Long> ids);

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

    boolean existLargeCorrection(int departmentId, int reportPeriodId, LocalDateTime correctionDate);

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
     * @param pagingParams - параметры пагинации
     * @return отчетные периоды подразделений
     */
    PagingResult<DepartmentReportPeriodJournalItem> findAll(DepartmentReportPeriodFilter departmentReportPeriodFilter, PagingParams pagingParams);

    /**
     * Поиск отчетного периода подразделения по его ID
     * @param id
     */
    DepartmentReportPeriod findOne(Long id);

    @Transactional(readOnly = false)
    DepartmentReportPeriod update(DepartmentReportPeriod departmentReportPeriodItem);
}
