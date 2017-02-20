package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;
import java.util.Map;

@ScriptExposed
public interface DepartmentService {
    /**
     * Подразделение по Id
     */
    Department get(Integer id);

    /**
     * Возвращает путь в иерархии до указанного подразделения.
     *
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchy(Integer departmentId);

    /**
     * Получить список ВСЕХ дочерних подразделений по коду подзаделения.
     *
     * @param parentDepartmentId идентификатор родительского подразделения
     * @return список объектов, представляющих текущее и дочерние подразделения
     */
    List<Department> getAllChildren(int parentDepartmentId);

    Integer getParentTBId(int departmentId);

    Department getBankDepartment();

    /**
     * Получение идентификаторов подразделений по типу
     * @param type тип подразделения
     * @return список идентификаторов подразделений
     */
    List<Integer> getDepartmentIdsByType(int type);

    /**
     * Получить подразделение
     */
    Department getDepartmentBySbrfCode(String sbrfCode, boolean activeOnly);

    /**
     * Получить подразделения по списку идентификаторов
     * @param departmentIds список идентификаторов
     * @return набор сочетаний идентификатор-подразделение
     */
    Map<Integer, Department> getDepartments(List<Integer> departmentIds);

    /**
     * Возвращает путь в иерархии до указанного подразделения использую краткое имя подразделения
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchyShortNames(Integer departmentId);
}
