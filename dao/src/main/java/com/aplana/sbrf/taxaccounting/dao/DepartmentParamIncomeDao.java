package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;

/**
 * DAO для работы с информацией по основным параметрам подразделения банка
 */
public interface DepartmentParamIncomeDao {
	/**
	 * Получить по id подразделения его параметры по налогу на прибыль
	 * @param departmentId идентфикатор подразделения
	 * @return параметры подразделения по налогу на прибыль
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParamIncome getDepartmentParamIncome(int departmentId);
}
