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
     * @return
     */
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
     * Выборка для бизнес-администрирования
     *
     * @param tAUserInfo пользователь
     * @return
     */
    List<Department> getBADepartments(TAUserInfo tAUserInfo);

    /**
     * Получение ТБ
     *
     * @param tAUserInfo пользователь
     * @return
     */
    List<Department> getTBDepartments(TAUserInfo tAUserInfo);

    /**
     * Получение Банка
     *
     * @return
     */
    Department getBankDepartment();

    /**
     * Выборка для доступа к экземплярам НФ/деклараций
     *
     * @param tAUserInfo пользователь
     * @return
     */
    List<Department> getTaxFormDepartments(TAUserInfo tAUserInfo);

    /**
     * Выборка для назначения подразделений-исполнителей
     *
     * @param tAUserInfo пользователь
     * @return
     */
    List<Department> getDestinationDepartments(TAUserInfo tAUserInfo);

    /**
     * Выборка для параметров печатной формы
     *
     * @param formData НФ
     * @return
     */
    List<Department> getPrintFormDepartments(FormData formData);

    /**
     * Выборка подразделений по открытым периодам
     *
     * @param tAUserInfo пользователь
     * @param reportPeriod  открытый период
     * @return
     */
    List<Department> getOpenPeriodDepartments(TAUserInfo tAUserInfo, ReportPeriod reportPeriod);

}
