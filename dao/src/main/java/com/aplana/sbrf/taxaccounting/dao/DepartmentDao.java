package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.Date;
import java.util.List;

/**
 * DAO для работы с информацией по подразделениям банка 
 */
public interface DepartmentDao {
	/**
	 * Получить подразделение по коду
	 * @param id идентфикатор подразделения
	 * @return объект подразделения пользователя
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если подразделение с таким идентификатором не существует
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
     * Получить список ВСЕХ дочерних подразделений по коду подзаделения
     * @param parentDepartmentId идентификатор родительского подразделения
     * @return список объектов, представляющих текущее и дочерние подразделения
     */
    List<Department> getAllChildren(int parentDepartmentId);

    List<Integer> getAllChildrenIds(int depId);

    /**
     * Получить список ВСЕХ дочерних подразделений по коду подзаделения
     * @param depId идентификатор родительского подразделения
     * @return список объектов, представляющих текущее и родительские подразделения
     */
    List<Integer> getAllParentIds(int depId);

	/**
	 * Получить родительское подразделение по коду подзаделения
	 * * Внимание: объект, возвращаемый данным методом инициализируется не полностью (в частности в ним не заполняется
	 * {@link Department#getFormTypeIds() информация по налоговым формам, с которыми работает данное подразделение}).
	 * @param departmentId идентификатор подразделения для которого нужно найти родительское подразделение
	 * @return
	 */
	Department getParent(int departmentId);

    /**
     * Возвращает путь в иерархии до указанного подразделения до корневого (не включительно),
     * если в параметр departmentId передается id корневого подразделения, то возвращается его наименование
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchy(Integer departmentId);

	/**
	 * Возвращает путь в иерархии до указанного подразделения используя сокращеные наименования
	 * @param departmentId подразделение до которого строится иерархия
	 * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
	 */
	String getParentsHierarchyShortNames(Integer departmentId);

    Integer getParentTBId(int departmentId);

    /**
     * Получить список всех подразделений
     * @return список всех подразделений
     */
    List<Department> listDepartments();

    /**
     * Получить список идентификаторов всех подразделений
     * @return список идентификаторов всех подразделений
     */
    List<Integer> listDepartmentIds();
        
    /**
     * Получение обособленного подразделения по значению 
     * «Код подразделения в нотации Сбербанка» 
     */
    Department getDepartmentBySbrfCode(String sbrfCode);

    /**
     * Подразделения по значению атрибута «Код подразделения»
     */
    Department getDepartmentByCode(int code);
    
    /**
     * Получение обособленного подразделения по значению 
     * «Наименование подразделения»»
     */
    Department getDepartmentByName(String name);

    /**
     * Получение подразделений по типу
     * @param type тип подразделения
     * @return список подразделений
     */
    List<Department> getDepartmentsByType(int type);

    /**
     * Получение идентификаторов подразделений по типу
     * @param type тип подразделения
     * @return список идентификаторов подразделений
     */
    List<Integer> getDepartmentIdsByType(int type);

    /**
     * Получение ТБ для подразделения (тип = 2)
     * @param departmentId Подразделение пользователя
     * @return ТБ
     */
    Department getDepartmenTB(int departmentId);

    /**
     * Получение ТБ для подразделения (тип = 2) + все дочерние подразделения
     * @param departmentId Подразделение пользователя
     * @return Список подразделений
     */
    List<Department> getDepartmenTBChildren(int departmentId);

    /**
     * Получение ТБ для подразделения (тип = 2) + все дочерние подразделения
     * @param departmentId Подразделение пользователя
     * @return Список идентификаторов подразделений
     */
    List<Integer> getDepartmenTBChildrenId(int departmentId);

    /**
     * Получение списка подразделений, необходимых для построения неразрывного дерева подразделений
     * @param availableDepartments
     * @return
     */
    List<Department> getRequiredForTreeDepartments(List<Integer> availableDepartments);

    /**
     * Список подразделений, в которых доступны декларации/НФ (по иерархии подразделений и по связям источник-приемник)
     * Только для роли "Контролер"
     * http://conf.aplana.com/pages/viewpage.action?pageId=11380670
     * @param userDepartmentId Подразделение пользователя
     * @param taxTypes Типы налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return Список id доступных подразделений
     */
    List<Integer> getDepartmentsBySourceControl(int userDepartmentId, List<TaxType> taxTypes, Date periodStart, Date periodEnd);

    /**
     * Список подразделений, в которых доступны декларации/НФ (по иерархии подразделений и по связям источник-приемник)
     * Только для роли "Контролер НС"
     * http://conf.aplana.com/pages/viewpage.action?pageId=11380670
     * @param userDepartmentId Подразделение пользователя
     * @param taxTypes Типы налога
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return Список id доступных подразделений
     */
    List<Integer> getDepartmentsBySourceControlNs(int userDepartmentId, List<TaxType> taxTypes, Date periodStart, Date periodEnd);

    /**
     * Получение списка исполнителей по списку идентификаторов подразделений
     * @param departments список id'шников подразделений
     * @param formType id формы
     * @return писок id'шников подразделений (исполнителей)
     */
    List<Integer> getPerformers(List<Integer> departments, int formType);

	/**
	 * Получение списка исполнителей по списку идентификаторов подразделений и типам налога
	 * @param departments список id'шников подразделений
	 * @param taxTypes типы налогов
	 * @return писок id'шников подразделений (исполнителей)
	 */
	List<Integer> getPerformers(List<Integer> departments, List<TaxType> taxTypes);

    /**
     * Все подразделения, для форм которых, подразделения departments назначены исполнителями
     */
    List<Integer> getDepartmentIdsByExcutors(List<Integer> departments, List<TaxType> taxTypes);

    /**
     * Все подразделения, для форм которых, подразделения departments назначены исполнителями
     */
    List<Integer> getDepartmentIdsByExecutors(List<Integer> departments);

    List<Integer> getDepartmentsByName(String departmentName);

    /**
     * Используемое наименование подразделения для печати
     * @param departmentId id подразделения
     * @return строка наименования
     */
    String getReportDepartmentName(int departmentId);
}
