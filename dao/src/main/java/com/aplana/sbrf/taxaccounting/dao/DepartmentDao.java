package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * DAO для работы с информацией по подразделениям банка
 */
public interface DepartmentDao extends PermissionDao {
    /**
     * Получить подразделение по коду
     *
     * @param id идентфикатор подразделения
     * @return объект подразделения пользователя
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если подразделение с таким идентификатором не существует
     */
    Department getDepartment(int id);

    /**
     * Проверяет существует ли подразделение
     *
     * @param departmentId идентификатор подразделения
     * @return true если существует
     */
    boolean existDepartment(int departmentId);

    /**
     * Получить список дочерних подразделений по коду подзаделения
     * Внимание: объекты, возвращаемые данным методом инициализируются не полностью (в частности в них не заполняется
     * информация по налоговым формам, с которыми работает данное подразделение}).
     *
     * @param parentDepartmentId идентификатор родительского подразделения
     * @return список объектов, представляющих дочерние подразделения, если таковых нет, то будет возвращён пустой список
     */
    List<Department> getChildren(int parentDepartmentId);

    /**
     * Получить список ВСЕХ дочерних подразделений по коду подзаделения
     *
     * @param parentDepartmentId идентификатор родительского подразделения
     * @return список объектов, представляющих текущее и дочерние подразделения
     */
    List<Department> getAllChildren(int parentDepartmentId);

    /**
     * Получить список идентификаторов всех дочерних подразделений (вместе с родительским)
     *
     * @param parentDepartmentId Идентификатор родительского подразделения
     * @return Список идентификаторов всех дочерних подразделений
     */
    List<Integer> findAllChildrenIdsById(int parentDepartmentId);

    /**
     * Получить список идентификаторов всех подразделений, являющихся дочерниими для заданных
     *
     * @param parentDepartmentIds Идентификаторы родительских подразделений
     * @return Список идентификаторов всех дочерних подразделений
     */
    List<Integer> findAllChildrenIdsByIds(Collection<Integer> parentDepartmentIds);

    /**
     * Получить список идентификаторов подразделений, для которых заланное является дочерним. К списку добавляется само подразделение
     *
     * @param childDepartmentId Идентификатор дочернего подразделения
     * @return Список идентификаторов родительских подразделений
     */
    List<Integer> fetchAllParentIds(int childDepartmentId);

    /**
     * Возвращает путь в иерархии до указанного подразделения до корневого (не включительно),
     * если в параметр departmentId передается id корневого подразделения, то возвращается его наименование
     *
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchy(@NotNull Integer departmentId);

    /**
     * Возвращает путь в иерархии до указанного подразделения используя сокращеные наименования
     *
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchyShortNames(Integer departmentId);

    /**
     * Получение id родительского территориального банка
     *
     * @param departmentId идентификатор подразделения
     * @return id родительского ТБ
     */
    Integer getParentTBId(int departmentId);

    /**
     * Получение родительского территориального банка
     *
     * @param departmentId идентификатор подразделения
     * @return родительский территориальный банк
     */
    Department getParentTB(int departmentId);

    /**
     * Получить список всех подразделений
     *
     * @return список всех подразделений
     */
    List<Department> listDepartments();

    /**
     * Получить список идентификаторов всех подразделений
     *
     * @return список идентификаторов всех подразделений
     */
    List<Integer> fetchAllIds();

    /**
     * Получение обособленного подразделения по значению
     * «Код подразделения в нотации Сбербанка»
     */
    Department getDepartmentBySbrfCode(String sbrfCode, boolean activeOnly);

    /**
     * Получение обособленных подразделений по значению
     * «Код подразделения в нотации Сбербанка»
     */
    List<Department> getDepartmentsBySbrfCode(String sbrfCode, boolean activeOnly);

    /**
     * Получение подразделений по типу
     *
     * @param type тип подразделения
     * @return список подразделений
     */
    List<Department> getDepartmentsByType(int type);

    /**
     * Получение идентификаторов подразделений по типу
     *
     * @param type тип подразделения
     * @return список идентификаторов подразделений
     */
    List<Integer> getDepartmentIdsByType(int type);

    /**
     * Получение ТБ для подразделения (тип = 2)
     *
     * @param departmentId Подразделение пользователя
     * @return ТБ
     */
    Department getDepartmentTB(int departmentId);

    /**
     * Получение ТБ для подразделения по списку физлиц (тип = 2)
     * Реализовано для ускорения производительности скрипта формирования XSLX
     *
     * @param personIdList список идентификаторов физлиц
     * @return список значений Идентификатор физлица - Название ТБ
     */
    Map<Long, String> getDepartmentTBByPersonIdList(List<Long> personIdList);

    /**
     * Получение ТБ для подразделения (тип = 2) + все дочерние подразделения
     *
     * @param departmentId Подразделение пользователя
     * @return Список подразделений
     */
    List<Department> getDepartmentTBChildren(int departmentId);

    /**
     * Получение ТБ для подразделения (тип = 2) + все дочерние подразделения
     *
     * @param departmentId Подразделение пользователя
     * @return Список идентификаторов подразделений
     */
    List<Integer> getDepartmentTBChildrenId(int departmentId);

    /**
     * Получение списка подразделений, необходимых для построения неразрывного дерева подразделений
     */
    List<Department> getRequiredForTreeDepartments(List<Integer> availableDepartments);

    /**
     * Возвращяет список ид подразделений, на которые назначено исполнителем заданные подразделения
     *
     * @param performersIds Список подразделений-исполнителей
     * @return список подразделений, исполнителями форм которых являются заданные подразделения
     */
    List<Integer> findAllIdsByPerformerIds(List<Integer> performersIds);

    /**
     * Возвращяет список ид ТерБанков подразделений, на которые назначено исполнителем заданные подразделения
     *
     * @param performerDepartmentId ID подразделения, которое является исполнителем
     * @return список ID Территориальных банков подразделений, исполнителем макетов форм которых является заданное подразделение
     */
    List<Integer> findAllTBIdsByPerformerId(int performerDepartmentId);

    /**
     * Получение подразделений по их идентификаторам
     *
     * @param ids список идентификаторов
     * @return список {@link Department} или пустой список
     */
    List<Department> findAllByIdIn(Collection<Integer> ids);

    /**
     * Получение списка названий всех подразделений.
     *
     * @param name поисковая строка
     * @return список {@link DepartmentName} с полным числом записей
     */
    PagingResult<DepartmentName> searchDepartmentNames(String name, PagingParams pagingParams);

    /**
     * Получение краткой информации о всех тербанках.
     */
    PagingResult<DepartmentShortInfo> fetchAllTBShortInfo(String filter, PagingParams pagingParams);
}
