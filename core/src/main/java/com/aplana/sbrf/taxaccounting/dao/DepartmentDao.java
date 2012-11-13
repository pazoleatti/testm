package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Department;

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
}
