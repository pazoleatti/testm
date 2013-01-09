package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * DAO для работы с информацией по подразделениям банка 
 */
public interface DepartmentDao extends ScriptExposed {
	/**
	 * Получить подразделение по коду
	 * @param id идентфикатор подразделения
	 * @return объект подразделения пользователя
	 * @throws com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException если подразделение с таким идентификатором не существует
	 */
	Department getDepartment(int id);

	/**
	 * Получить список дочерних подразделений по коду подзаделения
	 * Внимение: объекты, возвращаемые данным методом инициализируются не полностью (в частности в них не заполняется  
	 * {@link Department#getFormTypeIds() информация по налоговым формам, с которыми работает данное подразделение}). 
	 * @param parentDepartmentId идентификатор родительского подразделения
	 * @return список объектов, представляющих дочерние подразделения, если таковых нет, то будет возвращён пустой список
	 */
	List<Department> getChildren(int parentDepartmentId);

    /**
     * Получить список всех подразделений
     * @return список всех подразделений
     */
    List<Department> listDepartments();
}
