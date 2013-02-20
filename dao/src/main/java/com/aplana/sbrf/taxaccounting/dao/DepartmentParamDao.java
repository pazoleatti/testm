package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;

import java.util.List;

/**
 * DAO для работы с информацией по основным параметрам подразделения банка
 */
public interface DepartmentParamDao {
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
}
