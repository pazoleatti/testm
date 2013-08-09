package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;

/**
 * DAO для работы с информацией по подразделениям банка 
 */
public interface DepartmentDao {
	/**
	 * Получить подразделение по коду
	 * @param id идентфикатор подразделения
	 * @return объект подразделения пользователя
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если подразделение с таким идентификатором не существует
	 */
	Department getDepartment(int id);

	/**
	 * Получить список дочерних подразделений по коду подзаделения
	 * Внимание: объекты, возвращаемые данным методом инициализируются не полностью (в частности в них не заполняется  
	 * {@link Department#getFormTypeIds() информация по налоговым формам, с которыми работает данное подразделение}). 
	 * @param parentDepartmentId идентификатор родительского подразделения
	 * @return список объектов, представляющих дочерние подразделения, если таковых нет, то будет возвращён пустой список
	 */
	List<Department> getChildren(int parentDepartmentId);

	/**
	 * Получить родительское подразделение по коду подзаделения
	 * * Внимание: объект, возвращаемый данным методом инициализируется не полностью (в частности в ним не заполняется
	 * {@link Department#getFormTypeIds() информация по налоговым формам, с которыми работает данное подразделение}).
	 * @param departmentId идентификатор подразделения для которого нужно найти родительское подразделение
	 * @return
	 */
	Department getParent(int departmentId);

    /**
     * Получить список всех подразделений
     * @return список всех подразделений
     */
    List<Department> listDepartments();
        
    /**
     * Получение обособленного подразделения по значению 
     * «Код подразделения в нотации Сбербанка» 
     */
    Department getDepartmentBySbrfCode(String sbrfCode);
    
    /**
     * Получение обособленного подразделения по значению 
     * ««Наименование подразделения»» 
     */
    Department getDepartmentByName(String name);
}
