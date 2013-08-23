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
	
	
	
	
}
