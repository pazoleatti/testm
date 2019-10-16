package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentName;
import com.aplana.sbrf.taxaccounting.model.DepartmentShortInfo;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Сервис содержит действия и проверки связанные с департаментом
 *
 * @author sgoryachkin
 */
public interface DepartmentService {
    /**
     * Получить департамент
     *
     * @param departmentId
     * @return
     */
    Department getDepartment(int departmentId);

    boolean existDepartment(int departmentId);

    /**
     * Получить список всех департамент
     *
     * @return список всех департаментов
     */
    List<Department> listAll();

    /**
     * Получить список идентификаторов всех департамент
     *
     * @return список идентификаторов всех департаментов
     */
    List<Integer> listIdAll();

    /**
     * Получить дочерние подразделения (не полная инициализация)
     *
     * @param parentDepartmentId
     * @return
     */
    List<Department> getChildren(int parentDepartmentId);

    /**
     * 70 - Получить все дочерние подразделения, включая указанное
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=11381799">Аналитика</a>
     *
     * @param parentDepartmentId
     * @return
     */
    List<Department> getAllChildren(int parentDepartmentId);

    List<Integer> getAllChildrenIds(Integer depId);

    /**
     * Данная функция в качестве аргумента принимает список идентификаторов доступных пользователю департаментов, а возвращает
     * список департаментов, "размотанный" вверх по иерархии от каждого доступного пользователю департамента. Таким образом,
     * эта функция возвращает список департаментов, который необходим для построения полноценного дерева.
     *
     * @param availableDepartments список доступных пользователю департаментов
     * @return список департаментов, необходимый для построения дерева
     */
    Map<Integer, Department> getRequiredForTreeDepartments(Set<Integer> availableDepartments);

    /**
     * Данная функция возвращает список департаментов Таким образом,
     * эта функция возвращает список департаментов, который необходим для построения полноценного дерева.
     *
     * @return список департаментов
     */
    List<Department> listDepartments();

    /**
     * Получить подразделение
     */
    Department getDepartmentBySbrfCode(String sbrfCode, boolean activeOnly);

    /**
     * Получить подразделения
     */
    List<Department> getDepartmentsBySbrfCode(String sbrfCode, boolean activeOnly);

    /**
     * 10 - Выборка подразделений для бизнес-администрирования
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=11380675">Аналитика</a>
     *
     * @param tAUser пользователь
     * @return
     */
    List<Department> getBADepartments(TAUser tAUser, TaxType taxType);

    /**
     * 10 - Выборка идентификаторов подразделений для бизнес-администрирования
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=11380675">Аналитика</a>
     *
     * @param tAUser пользователь
     * @return список идентификаторов
     */
    List<Integer> getBADepartmentIds(TAUser tAUser);

    /**
     * 20 - Получение идентификаторов ТБ
     * Для роли "Контролер УНП" может быть несколько подразделений
     * Для роли "Контролер НС" только одно подразделение
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11380723">Аналитика</a>
     *
     * @param tAUser  пользователь
     * @param addRoot добавлять ли в список Контролеру УНП подразделение ПАО Сбербанк
     * @return список идентификаторов подразделений
     */
    List<Integer> getTBDepartmentIds(TAUser tAUser, TaxType taxType, boolean addRoot);

    /**
     * 30 - Получение Банка
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11381063">Аналитика</a>
     *
     * @return
     */
    Department getBankDepartment();

    /**
     * Возвращяет список идентификаторов подразделений, доступных пользователю.
     * <a href = "https://conf.aplana.com/pages/viewpage.action?pageId=41001785">аналитика</a>
     * - Для Контролера УНП: все подразделения;
     * - Для Контролера НС: все дочерние подразделения Тербанка пользователя и
     * все дочерние подразделения Тербанка, для которого хотя бы одно дочернее подразделение имеет исполнителем подразделение пользователя
     * - Для Оператора: все дочерние подразделения подразделения пользователя и
     * все дочерние подразделения подразделений, которые имеют исполнителем подразделение пользователя
     *
     * @param tAUser пользователь
     * @return список идентификаторов подразделений
     */
    List<Integer> findAllAvailableIds(TAUser tAUser);

    /**
     * Возвращяет список идентификаторов подразделений, доступных пользователю.
     * <a href = "https://conf.aplana.com/pages/viewpage.action?pageId=39175162">аналитика</a>
     * - Для Контролера УНП: все подразделения;
     * - Для Контролера НС или Оператора:
     *  1) все дочерние подразделения Тербанка пользователя
     *  2) все дочерние подразделения Тербанка, для которого хотя бы одно дочернее подразделение имеет исполнителем подразделение пользователя
     *
     * @param user пользователь
     * @return список идентификаторов подразделений
     */
    List<Integer> findAllAvailableTBIds(TAUser user);

    /**
     * 50 - Выборка id подразделений для назначения подразделений-исполнителей
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11380678">Аналитика</a>
     *
     * @param tAUser пользователь
     * @return
     */
    List<Department> getDestinationDepartments(TaxType taxType, TAUser tAUser);

    /**
     * Выборка id подразделений для назначения подразделений-исполнителей
     *
     * @param tAUser пользователь
     * @return Список id подразделений для назначения исполнителей
     */
    List<Integer> getDestinationDepartmentIds(TAUser tAUser);

    /**
     * Получить подразделения по списку идентификаторов
     *
     * @param departmentId список идентификаторов
     * @return набор сочетаний идентификатор-подразделение
     */
    Map<Integer, Department> getDepartments(List<Integer> departmentId);

    /**
     * Возвращает путь в иерархии до указанного подразделения
     *
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchy(Integer departmentId);

    /**
     * Возвращает путь в иерархии до указанного подразделения использую краткое имя подразделения
     *
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchyShortNames(Integer departmentId);

    /**
     * Получает родительский ТБ для подразделения.
     *
     * @param departmentId иденетификатор подразделения, для которого надо получить терр. банк.
     * @return терр. банк. Возвращает null, если departmentId корневое подразделение.
     * Возвращает переданное подразделение, если оно и есть террбанк.
     */
    Department getParentTB(int departmentId);

    /**
     * Получает ид родительского ТБ для подразделения.
     */
    Integer getParentTBId(int departmentId);

    /**
     * Получить списиок ТБ подразделений, для которых подразделение из ТБ пользователя является исполнителем макетов
     *
     * @param userTBDepId     подразделения-исполнителя
     * @param declarationType макет
     * @return
     */
    //List<Integer> getAllTBPerformers(int userTBDepId, DeclarationType declarationType);

    /**
     * Получение списка подразделений по идентификаторам
     *
     * @param ids список идентификаторов
     * @return список {@link Department} или пустой список
     */
    List<Department> findAllByIdIn(List<Integer> ids);

    /**
     * Получение подразделения по его коду
     *
     * @param code код подразделения
     * @return подразделение
     */
    Department findByCode(Long code);

    /**
     * Получение DTO всех подразделений.
     *
     * @param name поисковая строка
     * @return список {@link DepartmentName}
     */
    PagingResult<DepartmentName> searchDepartmentNames(String name, PagingParams pagingParams);

    /**
     * Получение краткой информации о всех тербанках.
     */
    PagingResult<DepartmentShortInfo> fetchAllTBShortInfo(String filter, PagingParams pagingParams);
}
