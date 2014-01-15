package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
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

    @Autowired
    DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

    @Autowired
    DepartmentFormTypeDao departmentFormTypeDao;

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
        Map<Integer, Department> departmentMap = new HashMap<Integer, Department>();

        List<Department> departmentList = availableDepartments == null ? this.listAll() :
                departmentDao.getRequiredForTreeDepartments(new ArrayList<Integer>(availableDepartments));

        for (Department department : departmentList) {
            departmentMap.put(department.getId(), department);
        }

        return departmentMap;
    }

    @Override
    public List<Department> listDepartments() {
        return departmentDao.listDepartments();
    }

    @Override
    public Department getDepartmentBySbrfCode(String sbrfCode) {
        return departmentDao.getDepartmentBySbrfCode(sbrfCode);
    }

    @Override
    public List<Department> getAllChildren(int parentDepartmentId) {
        return departmentDao.getAllChildren(parentDepartmentId);
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11380675
    @Override
    public List<Department> getBADepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();

        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmenTBChildren(tAUser.getDepartmentId()));
        }

        return retList;
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11380723
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
                Department departmenTB = departmentDao.getDepartmenTB(tAUser.getDepartmentId());
                if (departmenTB != null) {
                    retList.add(departmenTB);
                }
            }
        }

        return retList;
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11381063
    @Override
    public Department getBankDepartment() {
        // подразделение с типом 1
        return departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode()).get(0);
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
    @Override
    public List<Integer> getTaxFormDepartments(TAUser tAUser, TaxType taxType, boolean taxFormIsDeclaration) {
        List<Integer> retList = new ArrayList<Integer>();
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            for (Department dep : departmentDao.listDepartments()) {
                retList.add(dep.getId());
            }
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            retList.addAll(taxFormIsDeclaration ?
                    departmentDeclarationTypeDao.getDepartmentsBySourceControlNs(tAUser.getDepartmentId(), taxType) :
                    departmentFormTypeDao.getDepartmentsBySourceControlNs(tAUser.getDepartmentId(), taxType));

            retList.addAll(getExecutorsDepartments(retList));
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL)) {
            retList.addAll(taxFormIsDeclaration ?
                    departmentDeclarationTypeDao.getDepartmentsBySourceControl(tAUser.getDepartmentId(), taxType) :
                    departmentFormTypeDao.getDepartmentsBySourceControl(tAUser.getDepartmentId(), taxType));
            retList.addAll(getExecutorsDepartments(retList));
        } else if (tAUser.hasRole(TARole.ROLE_OPER) && !taxFormIsDeclaration) {
            // все дочерние подразделения для подразделения пользователя (включая его)
            for (Department dep : departmentDao.getAllChildren(tAUser.getDepartmentId())) {
                retList.add(dep.getId());
            }
            retList.addAll(getExecutorsDepartments(retList));
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set<Integer> setItems = new HashSet<Integer>(retList);
        retList.clear();
        retList.addAll(setItems);
        return retList;
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11380678
    @Override
    public List<Department> getDestinationDepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmenTBChildren(tAUser.getDepartmentId()));
            // подразделения с типом 3
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.GOSB.getCode()));
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set setItems = new HashSet(retList);
        retList.clear();
        retList.addAll(setItems);

        return retList;
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11381089
    @Override
    public List<Integer> getPrintFormDepartments(FormData formData) {
        List<Integer> retList = new ArrayList<Integer>();
        // подразделение, которому назначена налоговая форма
        retList.add(departmentDao.getDepartment(formData.getDepartmentId()).getId());
        // подразделения которые назначены исполнителями для налоговой формы
        retList.addAll(getExecutorsDepartments(retList));
        return retList;
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11383234
    @Override
    public List<Integer> getOpenPeriodDepartments(TAUser tAUser, TaxType taxType, boolean taxFormIsDeclaration, ReportPeriod reportPeriod) {
        List<Integer> retList = new ArrayList<Integer>();
        // Подразделения согласно выборке 40 - Выборка для доступа к экземплярам НФ/деклараций
        List<Integer> list = getTaxFormDepartments(tAUser, taxType, taxFormIsDeclaration);
        for (Integer departmentId : list) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(reportPeriod.getId(), departmentId.longValue());
            if (departmentReportPeriod != null && departmentReportPeriod.isActive()) {
                // Подразделения, для которых открыт указанный период
                retList.add(departmentId);
            }
        }

        Set setItems = new HashSet(retList);
        retList.clear();
        retList.addAll(setItems);

        return retList;
    }

    // TODO blocked by http://jira.aplana.com/browse/SBRFACCTAX-5397 (реализацию подразделений-исполнителей сейчас Денис Лошкарев только запускает в работу)
    private List<Integer> getExecutorsDepartments(List<Integer> departments) {
        List<Integer> retList = new ArrayList<Integer>();
        // TODO все подразделения, которые назначены исполнителями для форм подразделений списка departments
        return retList;
    }
}
