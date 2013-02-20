package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DepartmentParam;

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

}
