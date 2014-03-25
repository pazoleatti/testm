package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

public interface DepartmentReportPeriodDao {
	
	
	/**
	 * Получаем список по подразделению
	 * @param departmentId
	 * @return
	 */
	List<DepartmentReportPeriod> getByDepartment(Long departmentId);

	/**
	 * Получает список по подразделению и типу налога
	 * @param departmentId подразделение
	 * @param taxType тип налога
	 * @return список подразделение-отчетный период
	 */
	List<DepartmentReportPeriod> getByDepartmentAndTaxType(Long departmentId, TaxType taxType);
	
	/**
	 * Сохраняет DepartmentReportPeriod
	 * 
	 * @param departmentReportPeriod
	 */
	void save(DepartmentReportPeriod departmentReportPeriod);

    /**
     * Обновляет срок сдачи отчетности
     * @param reportPeriodId
     * @param departmentIds
     * @param deadline
     */
    void updateDeadline(int reportPeriodId, List<Integer> departmentIds, Date deadline);

	/**
	 * Открыть закрыть период для подразделения
	 * 
	 * @param reportPeriodId
	 * @param departmentId
	 * @param active
	 */
	void updateActive(int reportPeriodId, Long departmentId, boolean active);
	
	/**
	 * Получить объект
	 * 
	 * @param reportPeriodId
	 * @param departmentId
	 * @return
	 */
	DepartmentReportPeriod get(int reportPeriodId, Long departmentId);

	/**
	 * Удалить период
	 * @param reportPeriodId идентификатор отчетного периода
	 * @param departmentId подразделение, для которого удаляется период
	 */
	void delete(int reportPeriodId, Integer departmentId);

	/**
	 * Проверяет существование периода для подразделения
	 * @param departmentId подразделение, для которого осуществляется проверка существования периода
	 * @return true - существует, false - не существует
	 */
	boolean existForDepartment(Integer departmentId, long reportPeriodId);

	/**
	 * Получить признак активности периода для подразделения
	 * @param departmentId идентификатор подразделения
	 * @param reportPeriodId идентификатор отчетного периода
	 * @return  признак активности периода для подразделения
	 */
	boolean isPeriodOpen(int departmentId, long reportPeriodId);

    /**
     * Получить номер корректирующего периода.
     * @param reportPeriodId идентификатор отчетного период
     * @param departmentId идентификатор подразделения
     */
    int getCorrectionPeriodNumber(int reportPeriodId, long departmentId);
}
