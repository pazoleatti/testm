package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    DepartmentDao departmentDao;

    @Autowired
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired
    DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

    @Autowired
    PeriodService periodService;

    @Override
    public Department getDepartment(int departmentId) {
        return departmentDao.getDepartment(departmentId);
    }

    @Override
    public boolean existDepartment(int departmentId) {
        return departmentDao.existDepartment(departmentId);
    }

    @Override
    public List<Department> listAll() {
        return departmentDao.listDepartments();
    }

    @Override
    public List<Integer> listIdAll() {
        return departmentDao.fetchAllIds();
    }

    @Override
    public List<Department> getChildren(int parentDepartmentId) {
        return departmentDao.getChildren(parentDepartmentId);
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
    public Department getDepartmentBySbrfCode(String sbrfCode, boolean activeOnly) {
        return departmentDao.getDepartmentBySbrfCode(sbrfCode, activeOnly);
    }

    @Override
    public List<Department> getDepartmentsBySbrfCode(String sbrfCode, boolean activeOnly) {
        return departmentDao.getDepartmentsBySbrfCode(sbrfCode, activeOnly);
    }

    @Override
    public List<Department> getAllChildren(int parentDepartmentId) {
        return departmentDao.getAllChildren(parentDepartmentId);
    }

    @Override
    public List<Integer> getAllChildrenIds(Integer depId) {
        if (depId == null)
            return new ArrayList<Integer>(0);
        return departmentDao.fetchAllChildrenIds(depId);
    }

    @Override
    public List<Department> getBADepartments(TAUser tAUser, TaxType taxType) {
        List<Department> retList = new ArrayList<Department>();

        if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_UNP) ||
                tAUser.hasRole(taxType, TARole.F_ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_NS) ||
                tAUser.hasRole(taxType, TARole.F_ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmentTBChildren(tAUser.getDepartmentId()));
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_OPER) ||
                tAUser.hasRole(taxType, TARole.F_ROLE_OPER)) {
            retList.addAll(departmentDao.getAllChildren(tAUser.getDepartmentId()));
        }

        return retList;
    }

    @Override
    public List<Integer> getBADepartmentIds(TAUser tAUser) {
        List<Integer> retList = new ArrayList<Integer>();

        if (tAUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.fetchAllIds());
        } else if (tAUser.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmentTBChildrenId(tAUser.getDepartmentId()));
        } else if (tAUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            retList.addAll(departmentDao.fetchAllChildrenIds(tAUser.getDepartmentId()));
        }

        return retList;
    }

    @Override
    public List<Department> getTBDepartments(TAUser tAUser, TaxType taxType) {
        List<Department> retList = new ArrayList<Department>();

        if (tAUser.hasRole(TARole.ROLE_ADMIN)
                || tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_UNP) || tAUser.hasRole(taxType, TARole.F_ROLE_CONTROL_UNP)) {
            // подразделение с типом 1
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode()));
            // подразделение с типом 2
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.TERR_BANK.getCode()));
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_NS) || tAUser.hasRole(taxType, TARole.F_ROLE_CONTROL_NS)
                || tAUser.hasRole(taxType, TARole.N_ROLE_OPER) || tAUser.hasRole(taxType, TARole.F_ROLE_OPER)) {
            if (departmentDao.getDepartment(tAUser.getDepartmentId()).getType() == DepartmentType.CSKO_PCP) {
                List<Integer> departmentIds = departmentDao.getDepartmentIdsByExecutors(Arrays.asList(tAUser.getDepartmentId()));
                for (Integer depId : departmentIds) {
                    Department department = getParentTB(depId);
                    if (department != null) {
                        retList.add(department);
                    }
                }
            } else if (departmentDao.getDepartment(tAUser.getDepartmentId()).getType() == DepartmentType.TERR_BANK) {
                // поразделение пользователя
                retList.add(departmentDao.getDepartment(tAUser.getDepartmentId()));

            } else {
                // подразделение с типом 2, являющееся родительским по отношению к подразделению пользователя
                Department departmenTB = departmentDao.getDepartmentTB(tAUser.getDepartmentId());
                if (departmenTB != null) {
                    retList.add(departmenTB);
                }
            }
        }

        return retList;
    }

    @Override
    public List<Integer> getTBDepartmentIds(TAUser tAUser, TaxType taxType, boolean addRoot) {
        List<Integer> retList = new ArrayList<Integer>();

        if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_UNP) || tAUser.hasRole(taxType, TARole.F_ROLE_CONTROL_UNP)) {
            if (addRoot) {
                // подразделение с типом 1
                retList.addAll(departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode()));
            }
            // подразделения с типом 2
            retList.addAll(departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode()));
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_NS) || tAUser.hasRole(taxType, TARole.F_ROLE_CONTROL_NS)) {
            if (departmentDao.getDepartment(tAUser.getDepartmentId()).getType() == DepartmentType.TERR_BANK) {
                // поразделение пользователя
                retList.add(tAUser.getDepartmentId());
            } else {
                // подразделение с типом 2, являющееся родительским по отношению к подразделению пользователя
                Department departmenTB = departmentDao.getDepartmentTB(tAUser.getDepartmentId());
                if (departmenTB != null) {
                    retList.add(departmenTB.getId());
                }
            }
        }

        return retList;
    }

    @Override
    public Department getBankDepartment() {
        // подразделение с типом 1
        return departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode()).get(0);
    }

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENT, key = "'user_departments_'+#tAUser.id")
    public List<Integer> getTaxFormDepartments(TAUser tAUser) {TaxType taxType = TaxType.NDFL;
        List<Integer> retList = new ArrayList<>();
        if (tAUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            // Все подразделения из справочника подразделений
            retList.addAll(departmentDao.fetchAllIds());
        } else if (tAUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {
            Set<Integer> departments = new LinkedHashSet<>();
            // ТБ подразделения пользователя + все дочерние
            departments.addAll(departmentDao.getDepartmentTBChildrenId(tAUser.getDepartmentId()));
            // ТБ + дочерние подразделения, для которых подразделение пользователя назначено исполнителем. Т.е сначала вверх до ТБ, а потом все дочерние
            List<Integer> forPerform = departmentDao.getTBDepartmentIdsByDeclarationPerformer(tAUser.getDepartmentId());
            for (Integer tbId : forPerform) {
                departments.addAll(departmentDao.getDepartmentTBChildrenId(tbId));
            }
            retList.addAll(departments);
        } else if (tAUser.hasRoles(taxType, TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            // Дочерние подразделения для подразделения пользователя
            List<Integer> userDepartmentChildrenIds = departmentDao.fetchAllChildrenIds(tAUser.getDepartmentId());
            // Подразделения, исполнителями налоговых форм которых являются подразделение пользователя и его дочерние подразделения, и их дочерние подразделения
            List<Integer> declarationDepartmentsIds = departmentDao.fetchAllIdsByDeclarationsPerformers(userDepartmentChildrenIds);
            List<Integer> declarationDepartmentsChildrenIds = departmentDao.fetchAllChildrenIds(declarationDepartmentsIds);
            // В итоговый список входят дочерние подразделения для подразделения пользователя, подразделения, для форм которых они
            // назначены исполнителями, их дочерние подразделения
            retList.addAll(userDepartmentChildrenIds);
            retList.addAll(declarationDepartmentsChildrenIds);
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set<Integer> setItems = new HashSet<>(retList);
        retList.clear();
        retList.addAll(setItems);
        return retList;
    }

    @Override
    public List<Department> getDestinationDepartments(TaxType taxType, TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();
        if (tAUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set<Department> setItems = new HashSet<Department>(retList);
        retList.clear();
        retList.addAll(setItems);

        return retList;
    }

    @Override
    public List<Integer> getDestinationDepartmentIds(TAUser tAUser) {
        List<Integer> retList = new ArrayList<>();
        if (tAUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.fetchAllIds());
        }
        return retList;
    }

    @Override
    public List<Integer> getOpenPeriodDepartments(TAUser tAUser, TaxType taxType, int reportPeriodId) {
        List<Integer> retList = new ArrayList<Integer>();
        // Подразделения согласно выборке 40 - Выборка для доступа к экземплярам НФ/деклараций
        List<Integer> list = getTaxFormDepartments(tAUser);
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setIsActive(true);
        departmentReportPeriodFilter.setDepartmentIdList(list);
        departmentReportPeriodFilter.setReportPeriodIdList(Collections.singletonList(reportPeriodId));
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodDao.fetchAllByFilter(departmentReportPeriodFilter);
        for (DepartmentReportPeriod departmentReportPeriod : departmentReportPeriodList) {
            retList.add(departmentReportPeriod.getDepartmentId());
        }

        Set<Integer> setItems = new HashSet<Integer>(retList);
        retList.clear();
        retList.addAll(setItems);

        return retList;
    }

    @Override
    public Map<Integer, Department> getDepartments(List<Integer> departmentId) {
        Map<Integer, Department> result = new HashMap<Integer, Department>();
        for (Integer depId : departmentId) {
            Department department = departmentDao.getDepartment(depId);
            result.put(department.getId(), department);
        }
        return result;
    }

    @Override
    public String getParentsHierarchy(Integer departmentId) {
        return departmentDao.getParentsHierarchy(departmentId);
    }

    @Override
    public String getParentsHierarchyShortNames(Integer departmentId) {
        return departmentDao.getParentsHierarchyShortNames(departmentId);
    }

    @Override
    public Department getParentTB(int departmentId) {
        try {
            return departmentDao.getParentTB(departmentId);
        } catch (ServiceException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public List<Department> fetchAllDepartmentByIds(List<Integer> ids){
        return departmentDao.fetchAllDepartmentByIds(ids);
    }
}
