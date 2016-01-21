package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.springframework.beans.factory.annotation.Autowired;
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
    DepartmentFormTypeDao departmentFormTypeDao;

    @Autowired
    FormDataDao formDataDao;

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
    public Department getDepartmentBySbrfCode(String sbrfCode) {
        return departmentDao.getDepartmentBySbrfCode(sbrfCode);
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
    public List<Department> getBADepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();

        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmentTBChildren(tAUser.getDepartmentId()));
        }

        return retList;
    }

    @Override
    public List<Integer> getBADepartmentIds(TAUser tAUser) {
        List<Integer> retList = new ArrayList<Integer>();

        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartmentIds());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmentTBChildrenId(tAUser.getDepartmentId()));
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL) || tAUser.hasRole(TARole.ROLE_OPER)){
            retList.addAll(departmentDao.getAllChildrenIds(tAUser.getDepartmentId()));
        }

        return retList;
    }

    @Override
    public List<Department> getTBDepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();

        if (tAUser.hasRole(TARole.ROLE_ADMIN)
         || tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // подразделение с типом 1
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.ROOT_BANK.getCode()));
            // подразделение с типом 2
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.TERR_BANK.getCode()));
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)
                || tAUser.hasRole(TARole.ROLE_CONTROL)
                || tAUser.hasRole(TARole.ROLE_OPER)) {
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
    public List<Integer> getTBDepartmentIds(TAUser tAUser) {
        List<Integer> retList = new ArrayList<Integer>();

        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // подразделение с типом 1
            retList.addAll(departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode()));
            // подразделения с типом 2
            retList.addAll(departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode()));
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
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
    public List<Integer> getTaxFormDepartments(TAUser tAUser, List<TaxType> taxTypes, Date periodStart, Date periodEnd) {
        List<Integer> retList = new ArrayList<Integer>();
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // Все подразделения из справочника подразделений
            for (Department dep : departmentDao.listDepartments()) {
                retList.add(dep.getId());
            }
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            // 1, 2
            retList.addAll(departmentDao.getDepartmentsBySourceControlNs(tAUser.getDepartmentId(), taxTypes, periodStart, periodEnd));
            // 3
            retList.addAll(getDepartmentIdsByExecutors(retList, taxTypes));
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL)) {
            // 1, 2
            retList.addAll(departmentDao.getDepartmentsBySourceControl(tAUser.getDepartmentId(), taxTypes, periodStart, periodEnd));
            // 3
            retList.addAll(getDepartmentIdsByExecutors(retList, taxTypes));
        } else if (tAUser.hasRole(TARole.ROLE_OPER)) {
            // 1
            for (Department dep : departmentDao.getAllChildren(tAUser.getDepartmentId())) {
                retList.add(dep.getId());
            }
            // 2
            retList.addAll(getDepartmentIdsByExecutors(retList, taxTypes));
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set<Integer> setItems = new HashSet<Integer>(retList);
        retList.clear();
        retList.addAll(setItems);
        return retList;
    }

    @Override
    public List<Department> getSourcesDepartments(TAUser tAUser, Date periodStart, Date periodEnd) {
        List<Department> retList = new ArrayList<Department>();
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS) || tAUser.hasRole(TARole.ROLE_CONTROL)) {
            // подразделения, которым назначены формы, которые являются источниками данных для форм, назначенных подразделениям из 10 - Выборка для бизнес-администрирования.
            List<Integer> baDepartmentIds = getBADepartmentIds(tAUser);
            if (!baDepartmentIds.isEmpty()) {
                retList.addAll(departmentDao.getDepartmentsByDestinationSource(baDepartmentIds, periodStart, periodEnd));
            }
        }

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
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartmentIds());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS) || tAUser.hasRole(TARole.ROLE_CONTROL)) {
            // подразделения, которым назначены формы, которые являются источниками данных для форм, назначенных подразделениям из 10 - Выборка для бизнес-администрирования.
            List<Integer> baDepartmentIds = getBADepartmentIds(tAUser);
            if (!baDepartmentIds.isEmpty()) {
                retList.addAll(departmentDao.getDepartmentIdsByDestinationSource(baDepartmentIds, periodStart, periodEnd));
            }
        }

        return retList;
    }

    @Override
    public List<Department> getDestinationDepartments(TAUser tAUser) {
        List<Department> retList = new ArrayList<Department>();
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP) || tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
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
        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            for (Department dep : departmentDao.listDepartments()) {
                retList.add(dep.getId());
            }
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS) || tAUser.hasRole(TARole.ROLE_CONTROL) || tAUser.hasRole(TARole.ROLE_OPER)) {
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
    public List<Integer> getPrintFormDepartments(FormData formData) {
        List<Integer> retList = new ArrayList<Integer>();
        // подразделение, которому назначена налоговая форма
        retList.add(departmentDao.getDepartment(formData.getDepartmentId()).getId());
        // подразделения которые назначены исполнителями для налоговой формы
        retList.addAll(getExecutorsDepartments(retList, formData.getFormType().getId()));
        return retList;
    }

    @Override
    public List<Integer> getOpenPeriodDepartments(TAUser tAUser, List<TaxType> taxTypes, int reportPeriodId) {
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        List<Integer> retList = new ArrayList<Integer>();
        // Подразделения согласно выборке 40 - Выборка для доступа к экземплярам НФ/деклараций
        List<Integer> list = getTaxFormDepartments(tAUser, taxTypes, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        for (Integer departmentId : list) {
            // Открытый период подразделения для пары «Подразделение — Отчетный период» может быть только один
            DepartmentReportPeriodFilter departmentReportPeriodFilter = new DepartmentReportPeriodFilter();
            departmentReportPeriodFilter.setIsActive(true);
            departmentReportPeriodFilter.setDepartmentIdList(Arrays.asList(departmentId));
            departmentReportPeriodFilter.setReportPeriodIdList(Arrays.asList(reportPeriodId));
            List<DepartmentReportPeriod> departmentReportPeriodList = departmentReportPeriodDao.getListByFilter(departmentReportPeriodFilter);

            DepartmentReportPeriod departmentReportPeriod = null;
            if (departmentReportPeriodList.size() == 1) {
                departmentReportPeriod = departmentReportPeriodList.get(0);
            }
            if (departmentReportPeriod != null) {
                // Подразделения, для которых открыт указанный период
                retList.add(departmentId);
            }
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
        } catch (ServiceException e){
            throw new ServiceException("", e);
        }
    }

    @Override
    public Department getFormDepartment(Long formDataId) {
        return departmentDao.getDepartment(formDataDao.getWithoutRows(formDataId).getDepartmentId());
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

    private List<Integer> getExecutorsDepartments(List<Integer> departments, int formType) {
        return departmentDao.getPerformers(departments, formType);
    }

    /**
     * Все подразделения, для форм которых, подразделения departments назначены исполнителями
     */
    private List<Integer> getDepartmentIdsByExecutors(List<Integer> departments, List<TaxType> taxTypes) {
        return departmentDao.getDepartmentIdsByExecutors(departments, taxTypes);
    }
}
