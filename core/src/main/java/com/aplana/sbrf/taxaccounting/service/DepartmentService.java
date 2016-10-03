package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.*;

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
     * 70 - Получить все дочерние подразделения
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=11381799">Аналитика</a>
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
    Department getDepartmentBySbrfCode(String sbrfCode, boolean activeOnly);

    /**
     * 10 - Выборка подразделений для бизнес-администрирования
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=11380675">Аналитика</a>
     * @param tAUser пользователь
     * @return
     */
    List<Department> getBADepartments(TAUser tAUser);

    /**
     * 10 - Выборка идентификаторов подразделений для бизнес-администрирования
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=11380675">Аналитика</a>
     * @param tAUser пользователь
     * @return список идентификаторов
     */
    List<Integer> getBADepartmentIds(TAUser tAUser);

    /**
     * 20 - Получение ТБ универсальное
     * Для роли "Контролер УНП" может быть несколько подразделений
     * Для роли "Контролер НС" только одно подразделение
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=11380723">Аналитика</a>
     *
     * @param tAUser пользователь
     * @return список подразделений
     */
    List<Department> getTBDepartments(TAUser tAUser);

    /**
     * 25 - Получение ТБ пользователя. Может быть не более одного подразделения
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=11380666">Аналитика</a>
     * @param tAUser пользователь
     * @return список подразделений
     */
    List<Department> getTBUserDepartments(TAUser tAUser);

    /**
     * 20 - Получение идентификаторов ТБ
     * Для роли "Контролер УНП" может быть несколько подразделений
     * Для роли "Контролер НС" только одно подразделение
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11380723">Аналитика</a>
     * @param tAUser пользователь
     * @return список идентификаторов подразделений
     */
    List<Integer> getTBDepartmentIds(TAUser tAUser);

    /**
     * 30 - Получение Банка
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11381063">Аналитика</a>
     * @return
     */
    Department getBankDepartment();

    /**
     * 40 - Выборка id подразделений для доступа к экземплярам НФ/деклараций
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11380670">Аналитика</a>
     * @param tAUser пользователь
     * @param taxTypes Типы
     * @param periodStart  начало периода, в котором действуют назначения
     * @param periodEnd    окончание периода, в котором действуют назначения
     * @return
     */
    List<Integer> getTaxFormDepartments(TAUser tAUser, List<TaxType> taxTypes, Date periodStart, Date periodEnd);

    /**
     * 45 - Подразделения, доступные через назначение источников-приёмников
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=14816445">Аналитика</a>
     * @param tAUser       пользователь
     * @param periodStart  дата начала периода
     * @param periodEnd    дата окончания периода
     * @return
     */
    List<Department> getSourcesDepartments(TAUser tAUser, Date periodStart, Date periodEnd);

    /**
     * 45 - Идентификаторы подразделений, доступные через назначение источников-приёмников
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=14816445">Аналитика</a>
     * @param tAUser       пользователь
     * @param periodStart  дата начала периода
     * @param periodEnd    дата окончания периода
     * @return
     */
    Collection<Integer> getSourcesDepartmentIds(TAUser tAUser, Date periodStart, Date periodEnd);

    /**
     * 50 - Выборка id подразделений для назначения подразделений-исполнителей
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11380678">Аналитика</a>
     * @param tAUser пользователь
     * @return
     */
    List<Department> getDestinationDepartments(TAUser tAUser);

    /**
     * 55 - Подразделения, доступные через назначение исполнителя
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=14814963">Аналитика</a>
     * @param tAUser пользователь
     * @return
     */
    Collection<Integer> getAppointmentDepartments(TAUser tAUser);

    /**
     * 60 - Выборка id подразделений для параметров печатной формы
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11381089">Аналитика</a>
     * @param formData НФ
     * @return
     */
    List<Integer> getPrintFormDepartments(FormData formData);

    /**
     * 80 - Выборка id подразделений по открытым периодам
     * <a href = "http://conf.aplana.com/pages/viewpage.action?pageId=11383234">Аналитика</a>
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

    /**
     * Переключание флага использования подразделения модулем Гарантий
     * @param depId ид подразделения
     * @param used true - используется, false - не используется
     */
    void setUsedByGarant(int depId, boolean used);

    /**
     * Используемое наименование подразделения для печати
     * @param departmentId id подразделения
     * @return строка наименования
     */
    String getReportDepartmentName(int departmentId);

    int getHierarchyLevel(int departmentId);
}
