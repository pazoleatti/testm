package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private ReportPeriodDao reportPeriodDao;

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
        return departmentDao.listDepartmentIds();
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
    public List<Department> getAllChildren(int parentDepartmentId) {
        return departmentDao.getAllChildren(parentDepartmentId);
    }

    @Override
    public List<Integer> getAllChildrenIds(Integer depId) {
        if (depId == null)
            return new ArrayList<Integer>(0);
        return departmentDao.getAllChildrenIds(depId);
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
            retList.addAll(departmentDao.listDepartmentIds());
        } else if (tAUser.hasRoles(TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmentTBChildrenId(tAUser.getDepartmentId()));
        } else if (tAUser.hasRoles(TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            retList.addAll(departmentDao.getAllChildrenIds(tAUser.getDepartmentId()));
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
    public Department getParentDepartmentByType(int departmentId, DepartmentType type) {
        return departmentDao.getParentDepartmentByType(departmentId, type);
    }

    @Override
    public List<Department> getTBUserDepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();
        Department userDepartment = departmentDao.getDepartment(tAUser.getDepartmentId());
        if (userDepartment.getType() == DepartmentType.TERR_BANK) {
            // Если пользователю назначено подразделение, которое имеет тип 2, то оно должно быть результатом выборки
            retList.add(userDepartment);
        } else {
            // Если пользователю назначено подразделение, которое имеет тип не 2, то результатом выборки должно быть
            // подразделение, которое является родительским (любого уровня вверх) для подразделения пользователя и
            // имеет тип 2
            retList.add(getParentTB(tAUser.getDepartmentId()));
        }
        return retList;
    }

    @Override
    public List<Integer> getTBDepartmentIds(TAUser tAUser, TaxType taxType) {
        List<Integer> retList = new ArrayList<Integer>();

        if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_UNP) || tAUser.hasRole(taxType, TARole.F_ROLE_CONTROL_UNP)) {
            // подразделение с типом 1
            retList.addAll(departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode()));
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
    public List<Integer> getTaxFormDepartments(TAUser tAUser, TaxType taxType, LocalDateTime periodStart, LocalDateTime periodEnd) {
        List<Integer> retList = new ArrayList<Integer>();
        if (tAUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            // Все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartmentIds());
        } else if (tAUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS)) {
            Set<Integer> departments = new LinkedHashSet<Integer>();
            departments.addAll(departmentDao.getDepartmentTBChildrenId(tAUser.getDepartmentId()));
            for (int tbId : getTBDepartmentIdsByDeclarationPerformer(departmentDao.getParentTBId(tAUser.getDepartmentId()))) {
                departments.addAll(departmentDao.getDepartmentTBChildrenId(tbId));
            }
            retList.addAll(departments);
        } else if (tAUser.hasRoles(taxType, TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            //Дочерние подразделения для подразделения пользователя
            List<Integer> userDepartmentChildrenIds = departmentDao.getAllChildrenIds(tAUser.getDepartmentId());
            //Подразделения, исполнителями налоговых форм которых являются подразделение пользователя и его дочерние подразделения, и их дочерние подразделения
            List<Integer> declarationDepartmentsIds = departmentDao.getDepartmentsByDeclarationsPerformers(userDepartmentChildrenIds);
            List<Integer> declarationDepartmentsChildrenIds = departmentDao.getAllChildrenIds(declarationDepartmentsIds);
            //В итоговый список входят дочерние подразделения для подразделения пользователя, подразделения, для форм которых они
            //назначены исполнителями, их дочерние подразделения
            retList.addAll(userDepartmentChildrenIds);
            retList.addAll(declarationDepartmentsChildrenIds);
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set<Integer> setItems = new HashSet<Integer>(retList);
        retList.clear();
        retList.addAll(setItems);
        return retList;
    }

    @Override
    public List<Integer> getNDFLDeclarationDepartments(TAUser user) {
        int userDepartmentId = user.getDepartmentId();
        Set<Integer> departments = new LinkedHashSet<Integer>();
        if (user.hasRoles(TARole.N_ROLE_CONTROL_UNP)) {
            // Все подразделения из справочника подразделений
            departments.addAll(departmentDao.listDepartmentIds());
        } else if (user.hasRoles(TARole.N_ROLE_CONTROL_NS)) {
            departments.addAll(departmentDao.getDepartmentTBChildrenId(userDepartmentId));
            for (int tbId : getTBDepartmentIdsByDeclarationPerformer(departmentDao.getParentTBId(userDepartmentId))) {
                departments.addAll(departmentDao.getDepartmentTBChildrenId(tbId));
            }
        } else if (user.hasRoles(TARole.N_ROLE_OPER)) {
            //Дочерние подразделения для подразделения пользователя
            List<Integer> userDepartmentChildrenIds = departmentDao.getAllChildrenIds(userDepartmentId);
            //Подразделения, исполнителями налоговых форм которых являются подразделение пользователя и его дочерние подразделения, и их дочерние подразделения
            List<Integer> declarationDepartmentsIds = departmentDao.getDepartmentsByDeclarationsPerformers(userDepartmentChildrenIds);
            List<Integer> declarationDepartmentsChildrenIds = departmentDao.getAllChildrenIds(declarationDepartmentsIds);
            //В итоговый список входят дочерние подразделения для подразделения пользователя, подразделения, для форм которых они
            //назначены исполнителями, их дочерние подразделения
            departments.addAll(userDepartmentChildrenIds);
            departments.addAll(declarationDepartmentsChildrenIds);
        }
        return new ArrayList<Integer>(departments);
    }

    @Override
    public List<Department> getSourcesDepartments(TAUser tAUser, Date periodStart, Date periodEnd) {
        List<Department> retList = new ArrayList<Department>();
        /*
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS) || tAUser.hasRole(TARole.ROLE_CONTROL)) {
            // подразделения, которым назначены формы, которые являются источниками данных для форм, назначенных подразделениям из 10 - Выборка для бизнес-администрирования.
            List<Integer> baDepartmentIds = getBADepartmentIds(tAUser);
            if (!baDepartmentIds.isEmpty()) {
                retList.addAll(departmentDao.getDepartmentsByDestinationSource(baDepartmentIds, periodStart, periodEnd));
            }
        }*/

        // Результат выборки должен содержать только уникальные подразделения
        Set<Department> setItems = new HashSet<Department>(retList);
        retList.clear();
        retList.addAll(setItems);

        return retList;
    }

    @Override
    public Collection<Integer> getSourcesDepartmentIds(TAUser tAUser, Date periodStart, Date periodEnd) {
        // Результат выборки должен содержать только уникальные подразделения
        HashSet<Integer> retList = new HashSet<Integer>();
        /*
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartmentIds());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS) || tAUser.hasRole(TARole.ROLE_CONTROL)) {
            // подразделения, которым назначены формы, которые являются источниками данных для форм, назначенных подразделениям из 10 - Выборка для бизнес-администрирования.
            List<Integer> baDepartmentIds = getBADepartmentIds(tAUser);
            if (!baDepartmentIds.isEmpty()) {
                retList.addAll(departmentDao.getDepartmentIdsByDestinationSource(baDepartmentIds, periodStart, periodEnd));
            }
        }*/

        return retList;
    }

    @Override
    public List<Department> getDestinationDepartments(TaxType taxType, TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();
        if (tAUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS) ||
                tAUser.hasRoles(taxType, TARole.F_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_NS)) {
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
    public Collection<Integer> getAppointmentDepartments(TAUser tAUser) {
        // Результат выборки должен содержать только уникальные подразделения
        HashSet<Integer> retList = new HashSet<Integer>();
        TaxType taxType = TaxType.NDFL;
        if (tAUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            for (Department dep : departmentDao.listDepartments()) {
                retList.add(dep.getId());
            }
        } else if (tAUser.hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.F_ROLE_CONTROL_NS, TARole.N_ROLE_OPER, TARole.F_ROLE_OPER)) {
            // 1. подразделения, для форм которых подразделения из выборки 10 - Выборка для бизнес-администрирования назначены исполнителями.
            retList.addAll(departmentDao.getDepartmentIdsByExecutors(getBADepartmentIds(tAUser)));
            // 2. подразделения, для форм которых подразделения из выборки 45 - Подразделения, доступные через назначение источников-приёмников назначены исполнителями.
            for (Department dep : getSourcesDepartments(tAUser, null, null)) {
                retList.add(dep.getId());
            }
        }

        return retList;
    }

    @Override
    public List<Integer> getOpenPeriodDepartments(TAUser tAUser, TaxType taxType, int reportPeriodId) {
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        List<Integer> retList = new ArrayList<Integer>();
        // Подразделения согласно выборке 40 - Выборка для доступа к экземплярам НФ/деклараций
        List<Integer> list = getTaxFormDepartments(tAUser, taxType, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
        departmentReportPeriodFilter.setIsActive(true);
        departmentReportPeriodFilter.setDepartmentIdList(list);
        departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(reportPeriodId));
        List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);
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
    public List<Department> getDepartmentForSudir() {
        ArrayList<Department> departments = new ArrayList<Department>();
        departments.addAll(departmentDao.getDepartmentsByType(DepartmentType.CSKO_PCP.getCode()));
        departments.addAll(departmentDao.getDepartmentsByType(DepartmentType.MANAGEMENT.getCode()));
        return departments;
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
            Integer tbId = departmentDao.getParentTBId(departmentId);
            if (tbId == null)
                return null;
            return getDepartment(tbId);
        } catch (ServiceException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public void setUsedByGarant(int depId, boolean used) {
        departmentDao.setUsedByGarant(depId, used);
    }

    @Override
    public String getReportDepartmentName(int departmentId) {
        Department reportDepartment = getDepartment(departmentId);
        if (reportDepartment != null) {
            // рекурсивно проходим по родителям пока не упремся в корень, ТБ или никуда
            Integer parentId = reportDepartment.getParentId();
            Department parentDepartment = reportDepartment;
            while (parentDepartment != null && parentDepartment.getType() != DepartmentType.ROOT_BANK && parentDepartment.getType() != DepartmentType.TERR_BANK) {
                parentDepartment = getDepartment(parentId);
                if (parentDepartment != null) {
                    parentId = parentDepartment.getParentId();
                }
            }
            // если уперлись в ТБ, то выводим составное имя
            if (parentDepartment != null && reportDepartment.getType() != DepartmentType.TERR_BANK && parentDepartment.getType() == DepartmentType.TERR_BANK) {
                return parentDepartment.getName() + "/" + reportDepartment.getName();
            } else {
                // иначе только конец
                return reportDepartment.getName();
            }
        }
        return null;
    }

    @Override
    public int getHierarchyLevel(int departmentId) {
        return departmentDao.getHierarchyLevel(departmentId);
    }

    @Override
    public List<Integer> getTBDepartmentIdsByDeclarationPerformer(int performerDepartmentId) {
        return departmentDao.getTBDepartmentIdsByDeclarationPerformer(performerDepartmentId);
    }

    @Override
    public List<Integer> getTaxDeclarationDepartments(TAUser tAUser, DeclarationType declarationType) {
        TaxType taxType = TaxType.NDFL;
        if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_UNP)
                || tAUser.hasRole(taxType, TARole.F_ROLE_CONTROL_UNP)) {
            // Все подразделения из справочника подразделений
            return departmentDao.listDepartmentIds();
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_NS)) {
            // Все подразделения в рамках ТБ
            return departmentDao.getDepartmentTBChildrenId(tAUser.getDepartmentId());
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_OPER)) {
            List<Integer> departmentIds = new ArrayList<Integer>();
            // Подразделение + дочерниее
            departmentIds.addAll(departmentDao.getAllChildrenIds(tAUser.getDepartmentId()));
            // Подразделения для которых назначен источником
            departmentIds.addAll(departmentDao.getAllPerformers(tAUser.getDepartmentId(), declarationType.getId()));
            return departmentIds;
        }
        return new ArrayList<Integer>();
    }

    public List<Integer> getAllTBPerformers(int userTBDepId, DeclarationType declarationType) {
        return departmentDao.getAllTBPerformers(userTBDepId, declarationType.getId());
    }
}
