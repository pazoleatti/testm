package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Department;

import java.util.List;

/**
 * DAO для работы с информацией по подразделениям банка 
 */
public interface DepartmentDao {
	/**
	 * Получить подразделение по коду
	 * @param id идентфикатор подразделения
	 * @return объект подразделения пользователя
	 * @throws DaoException если подразделение с таким идентификатором не существует
	 */
	Department getDepartment(int id);

    /**
     * Получить список всех подразделений
     * @return список всех подразделений
     */
    List<Department> listDepartments();
}
