package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
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

	/**
	 * Получить по id подразделения его основные параметры
	 * @param departmentId идентфикатор подразделения
	 * @return основные параметры подразделения
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParam getDepartmentParam(int departmentId);

	/**
	 * Получить список всех обособленных подразделений.
	 * Подразделение считается обособленным, если у него есть настройки в таблице department_param
	 * @return список всех обособленных подразделений.
	 */
	List<Department> listIsolatedDepartments();

	/**
	 * Получить по id подразделения его параметры по налогу на прибыль
	 * @param departmentId идентфикатор подразделения
	 * @return параметры подразделения по налогу на прибыль
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParamIncome getDepartmentParamIncome(int departmentId);
}
