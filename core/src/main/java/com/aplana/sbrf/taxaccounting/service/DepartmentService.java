package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Сервис содержит действия и проверки связанные с департаментом
 * 
 * @author sgoryachkin
 *
 */
@ScriptExposed
public interface DepartmentService {
	

	/**
	 * Получить департамент
	 * 
	 * @param departmentId
	 * @return
	 */
	Department getDepartment(int departmentId);
	
	
	/**
	 * Получить дочерние подразделения (не полная инициализация)
	 * 
	 * @param parentDepartmentId
	 * @return
	 */
	List<Department> getChildren(int parentDepartmentId);
	
	


}
