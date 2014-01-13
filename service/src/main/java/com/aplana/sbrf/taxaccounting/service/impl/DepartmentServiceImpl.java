package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    public static final int UNP_ID = 1;

    @Autowired
    DepartmentDao departmentDao;

    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Override
    public Department getUNPDepartment() {
        return departmentDao.getDepartment(UNP_ID);
    }

    @Override
    public Department getDepartment(int departmentId) {
        return departmentDao.getDepartment(departmentId);
    }

    @Override
    public List<Department> listAll() {
        return departmentDao.listDepartments();
    }

    @Override
    public List<Department> getChildren(int parentDepartmentId) {
        return departmentDao.getChildren(parentDepartmentId);
    }

    @Override
    public Department getParent(int departmentId) {
        return departmentDao.getParent(departmentId);
    }

    @Override
    public Map<Integer, Department> getRequiredForTreeDepartments(Set<Integer> availableDepartments) {
        // TODO использовать древовидный запрос см. getAllChildren()
        Map<Integer, Department> departmentSet = new HashMap<Integer, Department>();

        // Если NULL то считаем что доступны все департаменты
        if (availableDepartments == null) {

            for (Department department : this.listAll()) {
                departmentSet.put(department.getId(), department);
            }

        } else { //Иначе рассчитываем необходимые для отображения

            for (Integer departmentId : availableDepartments) {
                departmentSet.put(departmentId, getDepartment(departmentId));
            }
            for (Integer departmentId : availableDepartments) {
                Integer searchFor = departmentId;
                while (true) {
                    Department department = getParent(searchFor);
                    if (department == null) {
                        break;
                    }
                    if (department.getParentId() == null || departmentSet.containsKey(department.getParentId())) {
                        departmentSet.put(department.getId(), department);
                        break;
                    } else {
                        departmentSet.put(department.getId(), department);
                        searchFor = department.getParentId();
                    }
                }
            }

        }
        return departmentSet;
    }

    @Override
    public List<Department> listDepartments() {
        return departmentDao.listDepartments();
    }

    @Override
    public Department getDepartmentBySbrfCode(String sbrfCode) {
        return departmentDao.getDepartmentBySbrfCode(sbrfCode);
    }


    /**
     * 70 - Дочерние подразделения
     * http://conf.aplana.com/pages/viewpage.action?pageId=11381799
     *
     * @param parentDepartmentId
     * @return
     */
    @Override
    public List<Department> getAllChildren(int parentDepartmentId) {
        return departmentDao.getAllChildren(parentDepartmentId);
    }

    /**
     * 10 - Выборка для бизнес-администрирования
     * http://conf.aplana.com/pages/viewpage.action?pageId=11380675
     *
     * @param tAUser
     * @return
     */
    @Override
    public List<Department> getBADepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();

        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());

        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            Department parentType2 = getDepartmentType2(tAUser.getDepartmentId());
            if (parentType2 != null) {
                retList.addAll(departmentDao.getAllChildren(parentType2.getId()));
            }
        }

        return retList;
    }

    /**
     * 20 - Получение ТБ
     * http://conf.aplana.com/pages/viewpage.action?pageId=11380723
     *
     * @param tAUser
     * @return
     */
    @Override
    public List<Department> getTBDepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();

        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // подразделение с типом 1
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode()));
            // подразделения с типом 2
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.TERBANK.getCode()));

        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            if (departmentDao.getDepartment(tAUser.getDepartmentId()).getType() == DepartmentType.TERBANK) {
                // поразделение пользователя
                retList.add(departmentDao.getDepartment(tAUser.getDepartmentId()));

            } else {
                // подразделение с типом 2, являющееся родительским по отношению к подразделению пользователя
                Department parentType2 = getDepartmentType2(tAUser.getDepartmentId());
                if (parentType2 != null) {
                    retList.add(parentType2);
                }
            }
        }

        return retList;
    }

    private Department getDepartmentType2(int departmentId) {
        Department parentType2 = departmentDao.getParent(departmentId);
        while (parentType2 != null && parentType2.getType() != DepartmentType.TERBANK) {
            parentType2 = departmentDao.getParent(parentType2.getId());
        }
        return parentType2;
    }

    /**
     * 30 - Получение Банка
     * http://conf.aplana.com/pages/viewpage.action?pageId=11381063
     *
     * @return
     */
    @Override
    public Department getBankDepartment() {
        // подразделение с типом 1
        return departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode()).get(0);
    }

    /**
     * 40 - Выборка для доступа к экземплярам НФ/деклараций
     * http://conf.aplana.com/pages/viewpage.action?pageId=11380670
     *
     * @param tAUser
     * @return
     */
    @Override
    public List<Department> getTaxFormDepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());

        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            Department parentType2 = getDepartmentType2(tAUser.getDepartmentId());
            if (parentType2 != null) {
                retList.addAll(departmentDao.getAllChildren(parentType2.getId()));
            }
            // TODO пп.2
            retList.addAll(getExecutorsDepartments(retList));

        } else if (tAUser.hasRole(TARole.ROLE_CONTROL)) {
            // все дочерние подразделения для подразделения пользователя
            retList.addAll(departmentDao.getAllChildren(tAUser.getDepartmentId()));
            // TODO пп.2 (тоже самое, что для ROLE_CONTROL_UNP)
            retList.addAll(getExecutorsDepartments(retList));

        } else if (tAUser.hasRole(TARole.ROLE_OPER)) {
            // все дочерние подразделения для подразделения пользователя (включая его)
            retList.addAll(departmentDao.getAllChildren(tAUser.getDepartmentId()));
            retList.addAll(getExecutorsDepartments(retList));
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set setItems = new HashSet(retList);
        retList.clear();
        retList.addAll(setItems);

        return retList;
    }

    /**
     * 50 - Выборка для назначения подразделений-исполнителей
     * http://conf.aplana.com/pages/viewpage.action?pageId=11380678
     *
     * @param tAUser пользователь
     * @return
     */
    @Override
    public List<Department> getDestinationDepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());

        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            Department parentType2 = getDepartmentType2(tAUser.getDepartmentId());
            if (parentType2 != null) {
                retList.addAll(departmentDao.getAllChildren(parentType2.getId()));
            }
            // подразделения с типом 3
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.GOSB.getCode()));
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set setItems = new HashSet(retList);
        retList.clear();
        retList.addAll(setItems);

        return retList;
    }

    /**
     * 60 - Выборка для параметров печатной формы
     * http://conf.aplana.com/pages/viewpage.action?pageId=11381089
     *
     * @param formData НФ
     * @return
     */
    @Override
    public List<Department> getPrintFormDepartments(FormData formData) {
        List<Department> retList = new ArrayList<Department>();
        // подразделение, которому назначена налоговая форма
        retList.add(departmentDao.getDepartment(formData.getDepartmentId()));
        // подразделения которые назначены исполнителями для налоговой формы
        retList.addAll(getExecutorsDepartments(retList));
        return retList;
    }

    /**
     * 80 - Выборка подразделений по открытым периодам
     * http://conf.aplana.com/pages/viewpage.action?pageId=11383234
     *
     * @param reportPeriod открытый период
     * @return
     */
    @Override
    public List<Department> getOpenPeriodDepartments(TAUser tAUser, ReportPeriod reportPeriod) {
        List<Department> retList = new ArrayList<Department>();
        // Подразделения согласно выборке 40 - Выборка для доступа к экземплярам НФ/деклараций
        List<Department> list = getTaxFormDepartments(tAUser);
        for (Department department : list) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(reportPeriod.getId(), Long.valueOf(department.getId()));
            if (departmentReportPeriod != null && departmentReportPeriod.isActive()) {
                // Подразделения, для которых открыт указанный период
                retList.add(department);
            }
        }

        Set setItems = new HashSet(retList);
        retList.clear();
        retList.addAll(setItems);

        return retList;
    }

    // TODO blocked by (реализацию подразделений-исполнителей сейчас Денис Лошкарев только запускает в работу)
    private List<Department> getExecutorsDepartments(List<Department> departments) {
        List<Department> retList = new ArrayList<Department>();
        // TODO все подразделения, которые назначены исполнителями для форм подразделений списка departments
        return retList;
    }
}
