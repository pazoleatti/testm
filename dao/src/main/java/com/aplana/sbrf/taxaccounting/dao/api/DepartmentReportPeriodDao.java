package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;

public interface DepartmentReportPeriodDao {
	
	
	/**
	 * Получаем список по подразделению
	 * @param departmentId
	 * @return
	 */
	List<DepartmentReportPeriod> getByDepartment(Long departmentId);
	
	/**
	 * Сохраняет DepartmentReportPeriod
	 * 
	 * @param departmentReportPeriod
	 */
	void save(DepartmentReportPeriod departmentReportPeriod);

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
	

	
}
