package com.aplana.sbrf.taxaccounting.service;

import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;

/**
 * Сервис содержит действия и проверки связанные с департаментом
 * 
 * @author sgoryachkin
 *
 */
public interface DepartmentService {
	

	/**
	 * Получить департамент
	 * 
	 * @param departmentId
	 * @return
	 */
	Department getDepartment(int departmentId);

	/**
	 * Получить список всех департамент
	 * @return список всех департаментов
	 */
	public List<Department> listAll();

	/**
	 * Получить дочерние подразделения (не полная инициализация)
	 * 
	 * @param parentDepartmentId
	 * @return
	 */
	List<Department> getChildren(int parentDepartmentId);


	/**
	 * Получить родительское подразделения для департамента
	 *
	 * @param departmentId
	 * @return
	 */
	Department getParent(int departmentId);

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
	List<Department> getIsolatedDepartments();

	/**
	 * Получить по id подразделения его параметры по налогу на прибыль
	 * @param departmentId идентфикатор подразделения
	 * @return параметры подразделения по налогу на прибыль
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParamIncome getDepartmentParamIncome(int departmentId);
	
	/**
	 * Получить по id подразделения его параметры по транспортному налогу
	 * @param departmentId идентфикатор подразделения
	 * @return параметры подразделения по налогу на прибыль
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	DepartmentParamTransport getDepartmentParamTransport(int departmentId);


	/**
	 * Данная функция в качестве аргумента принимает список идентификаторов доступных пользователю департаментов, а возвращает
	 * список департаментов, "размотанный" вверх по иерархии от каждого доступного пользователю департамента. Таким образом,
	 * эта функция возвращает список департаментов, который необходим для построения полноценного дерева.
	 * @param availableDepartments список доступных пользователю департаментов. Данный список получаем при вызове
	 *                             FormDataSearchService.getAvailableFilterValues().getDepartmentIds()
	 * @return список департаментов, необходимый для построения дерева
	 */
	Set<Department> getRequiredForTreeDepartments(Set<Integer> availableDepartments);
}
