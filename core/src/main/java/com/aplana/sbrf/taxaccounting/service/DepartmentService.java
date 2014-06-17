package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

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
     * Получаем подразделение UNP.
     * (Корень дерева, а не "Управление налогового планирования")
     *
     * @deprecated Необходимо пользоваться getBankDepartment()
     * @return
     */
    @Deprecated
    public Department getUNPDepartment();

    /**
     * Получить департамент
     *
     * @param departmentId
     * @return
     */
    Department getDepartment(int departmentId);

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
     * Получить все дочерние подразделения
     *
     * @param parentDepartmentId
     * @return
     */
    List<Department> getAllChildren(int parentDepartmentId);

    List<Integer> getAllChildrenIds(int depId);

    /**
     * Получить все родительские подразделения
     *
     * @param depId подразделение от которого ищем
     * @return список идентификаторов подразделений
     */
    List<Integer> getAllParentIds(int depId);

    /**
     * Получить родительское подразделения для департамента
     *
     * @param departmentId
     * @return
     */
    Department getParent(int departmentId);

    /**
     * Данная функция в качестве аргумента принимает список идентификаторов доступных пользователю департаментов, а возвращает
     * список департаментов, "размотанный" вверх по иерархии от каждого доступного пользователю департамента. Таким образом,
     * эта функция возвращает список департаментов, который необходим для построения полноценного дерева.
     *
     * @param availableDepartments список доступных пользователю департаментов. Данный список получаем при вызове
     *                             FormDataSearchService.getAvailableFilterValues().getDepartmentIds()
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
    Department getDepartmentBySbrfCode(String sbrfCode);

    /**
     * Выборка подразделений для бизнес-администрирования
     *
     * @param tAUser пользователь
     * @return
     */
    List<Department> getBADepartments(TAUser tAUser);

    /**
     * Выборка идентификаторов подразделений для бизнес-администрирования
     *
     * @param tAUser пользователь
     * @return список идентификаторов
     */
    List<Integer> getBADepartmentIds(TAUser tAUser);

    /**
     * Получение ТБ
     * http://conf.aplana.com/pages/viewpage.action?pageId=11380723
     * Для роли "Контролер УНП" может быть несколько подразделений
     * Для роли "Контролер НС" только одно подразделение
     *
     * @param tAUser пользователь
     * @return список подразделений
     */
    List<Department> getTBDepartments(TAUser tAUser);

    /**
     * Получение идентификаторов ТБ
     * http://conf.aplana.com/pages/viewpage.action?pageId=11380723
     * Для роли "Контролер УНП" может быть несколько подразделений
     * Для роли "Контролер НС" только одно подразделение
     *
     * @param tAUser пользователь
     * @return список идентификаторов подразделений
     */
    List<Integer> getTBDepartmentIds(TAUser tAUser);

    /**
     * Получение Банка
     *
     * @return
     */
    Department getBankDepartment();

    /**
     * Выборка id подразделений для доступа к экземплярам НФ/деклараций
     * @param tAUser пользователь
     * @param taxTypes Типы налога
     * @return
     */
    List<Integer> getTaxFormDepartments(TAUser tAUser, List<TaxType> taxTypes);

    /**
     * Выборка id подразделений для назначения подразделений-исполнителей
     *
     * @param tAUser пользователь
     * @return
     */
    List<Department> getDestinationDepartments(TAUser tAUser);

    /**
     * Выборка id подразделений для параметров печатной формы
     *
     * @param formData НФ
     * @return
     */
    List<Integer> getPrintFormDepartments(FormData formData);

    /**
     * Выборка id подразделений по открытым периодам
     *
     * @param tAUser пользователь
     * @param taxTypes Типы налога
     * @param reportPeriodId id периода
     * @return
     */
    List<Integer> getOpenPeriodDepartments(TAUser tAUser, List<TaxType> taxTypes, int reportPeriodId);

	/**
	 * Получить подразделения по списку идентификаторов
	 * @param departmentId список идентификаторов
	 * @return набор сочетаний идентификатор-подразделение
	 */
	Map<Integer, Department> getDepartments(List<Integer> departmentId);

    /**
     * Получает набор идентификаторов по имени департамента
     * @param departmentName наименование демартамента
     * @return коллекция идентификаторов департаментов
     */
    List<Integer> getDepartmentsByName(String departmentName);

    /**
     * Список подразделений передаваемых в СУДИР
     * @return подразделения с типом {@link com.aplana.sbrf.taxaccounting.model.DepartmentType#MANAGEMENT}
     */
    List<Department> getDepartmentForSudir();

    /**
     * Возвращает путь в иерархии до указанного подразделения
     * @param departmentId подразделение до которого строится иерархия
     * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
     */
    String getParentsHierarchy(Integer departmentId);

	/**
	 * Возвращает путь в иерархии до указанного подразделения использую краткое имя подразделения
	 * @param departmentId подразделение до которого строится иерархия
	 * @return строка вида "подразделение/другое подразделение/еще одно подразделение"
	 */
	String getParentsHierarchyShortNames(Integer departmentId);

    /**
     * Получает родительский ТБ для подразделения.
     * @param departmentId иденетификатор подразделения, для которого надо получить терр. банк.
     * @return терр. банк. Возвращает null, если departmentId корневое подразделение.
     *          Возвращает переданное подразделение, если оно и есть террбанк.
     */
    Department getParentTB(int departmentId);

    /**
     * Получить получить подразделение создавшее форму
     * @param formDataId идентификатор формы
     * @return подразделение создавшее форму
     */
    Department getFormDepartment(Long formDataId);


}
