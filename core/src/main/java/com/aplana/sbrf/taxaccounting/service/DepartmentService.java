package com.aplana.sbrf.taxaccounting.service;

import java.util.*;

import com.aplana.sbrf.taxaccounting.model.Department;

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
	List<Department> listAll();

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
	 * Получить список всех обособленных подразделений.
	 * Подразделение считается обособленным, если у него есть настройки в таблице department_param
	 * @return список всех обособленных подразделений.
	 */
	List<Department> getIsolatedDepartments();

	/**
	 * Данная функция в качестве аргумента принимает список идентификаторов доступных пользователю департаментов, а возвращает
	 * список департаментов, "размотанный" вверх по иерархии от каждого доступного пользователю департамента. Таким образом,
	 * эта функция возвращает список департаментов, который необходим для построения полноценного дерева.
	 * @param availableDepartments список доступных пользователю департаментов. Данный список получаем при вызове
	 *                             FormDataSearchService.getAvailableFilterValues().getDepartmentIds()
	 * @return список департаментов, необходимый для построения дерева
	 */
	Map<Integer, Department> getRequiredForTreeDepartments(Set<Integer> availableDepartments);
	
	/**
	 * Данная функция возвращает список департаментов Таким образом,
	 * эта функция возвращает список департаментов, который необходим для построения полноценного дерева.
	 * @return список департаментов
	 */
	List<Department> listDepartments();

	/**
	 * Получить подразделение
	 */
	Department getDepartmentBySbrfCode(String sbrfCode);
}
