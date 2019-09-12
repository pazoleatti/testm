package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Date;
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

    /**
     * Получить список идентификаторов подразделений, для которых заланное является дочерним. К списку добавляется само подразделение
     *
     * @param childDepartmentId Идентификатор дочернего подразделения
     * @return Список идентификаторов родительских подразделений
     */
    List<Integer> fetchAllParentDepartmentsIds(int childDepartmentId);

    Integer getParentTBId(int departmentId);

    Department getBankDepartment();

    /**
     * Получение идентификаторов подразделений по типу
     *
     * @param type тип подразделения
     * @return список идентификаторов подразделений
     */
    List<Integer> getDepartmentIdsByType(int type);

    /**
     * Получить подразделение
     */
    Department getDepartmentBySbrfCode(String sbrfCode, boolean activeOnly);

    /**
     * Получить подразделения
     */
    List<Department> getDepartmentsBySbrfCode(String sbrfCode, boolean activeOnly);

    /**
     * Получить подразделения по списку идентификаторов
     *
     * @param departmentIds список идентификаторов
     * @return набор сочетаний идентификатор-подразделение
     */
    Map<Integer, Department> getDepartments(List<Integer> departmentIds);

    /**
     * Возвращает путь в иерархии до указанного подразделения использую краткое имя подразделения
     *
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchyShortNames(Integer departmentId);

    /**
     * Получение родительского территориального банка
     *
     * @param departmentId идентификатор подразделения
     * @return родительский территориальный банк
     */
    Department getParentTB(int departmentId);

    /**
     * Получение родительского территориального банка по списку идентификаторов
     * источников при формировании консолидированной формы (тип = 2)
     * Реализовано для ускорения производительности скрипта формирования XSLX
     *
     * @param incomeSourceIdList список идентификаторов источников при формировании консолидированной формы
     * @return Cписок значений: Идентификатор источника - Название родительского территориального банка
     */
    Map<Long, String> getParentTBbyIncomeSourceIdList(List<Long> incomeSourceIdList);

    /**
     * Получить список ID Территориальных банков подразделений, исполнителем (без дочерних) макетов форм которых является заданное подразделение
     *
     * @param performerDepartmentId ID подразделения, которое является исполнителем
     * @return список ID Территориальных банков подразделений, исполнителем макетов форм которых является заданное подразделение
     */
    List<Integer> fetchAllTBIdsByPerformer(int performerDepartmentId);
}
