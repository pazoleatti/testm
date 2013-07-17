package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;

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
	 * Получить параметры по налогу на прибыль.
	 * @param departmentId
	 * @return параметры по налогу на прибыль
	 */
	DepartmentParamIncome getDepartmentParamIncome(int departmentId);
	
	/**
	 * Получить параметры по транспортному налогу. 
	 * @param departmentId
	 * @return параметры по транспортному налогу
	 */
	DepartmentParamTransport getDepartmentParamTransport(int departmentId);

    /**
     * Сохранение изменений в общих параметрах
     * @param param Общие параметры подразделения
     */
    void updateDepartmentParam(DepartmentParam param);

    /**
     * Сохранение изменений в параметрах по налогу на прибыль
     * @param param Параметры по налогу на прибыль
     */
    void updateDepartmentParamIncome(DepartmentParamIncome param);

    /**
     * Сохранение изменений в параметрах по транспортному налогу
     * @param param Параметры по транспортному налогу
     */
    void updateDepartmentParamTransport(DepartmentParamTransport param);
}
