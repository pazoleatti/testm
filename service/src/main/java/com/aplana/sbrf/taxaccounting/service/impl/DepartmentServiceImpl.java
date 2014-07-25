package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.PeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    /**
     * @deprecated См. getBankDepartment()
     */
    @Deprecated
    public static final int UNP_ID = 1;

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
    public List<Integer> listIdAll() {
        return departmentDao.listDepartmentIds();
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
    public Department getDepartmentByCode(int code) {
        return departmentDao.getDepartmentByCode(code);
    }

    @Override
    public List<Department> getAllChildren(int parentDepartmentId) {
        return departmentDao.getAllChildren(parentDepartmentId);
    }

    @Override
    public List<Integer> getAllChildrenIds(int depId) {
        if (depId == 0)
            return new ArrayList<Integer>(0);
        return departmentDao.getAllChildrenIds(depId);
    }

    @Override
    public List<Integer> getAllParentIds(int depId) {
        if (depId == 0)
            return new ArrayList<Integer>(0);
        return departmentDao.getAllParentIds(depId);
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

    @Override
    public List<Integer> getBADepartmentIds(TAUser tAUser) {
        List<Integer> retList = new ArrayList<Integer>();

        if (tAUser.hasRole(TARole.ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartmentIds());
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmenTBChildrenId(tAUser.getDepartmentId()));
        }

        return retList;
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11380723
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
                Department departmenTB = departmentDao.getDepartmenTB(tAUser.getDepartmentId());
                if (departmenTB != null) {
                    retList.add(departmenTB);
                }
            }
        }

        return retList;
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=13112983
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
                Department departmenTB = departmentDao.getDepartmenTB(tAUser.getDepartmentId());
                if (departmenTB != null) {
                    retList.add(departmenTB.getId());
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
            retList.addAll(getDepartmentIdsByExcutors(retList, taxTypes));
        } else if (tAUser.hasRole(TARole.ROLE_CONTROL)) {
            // 1, 2
            retList.addAll(departmentDao.getDepartmentsBySourceControl(tAUser.getDepartmentId(), taxTypes, periodStart, periodEnd));
            // 3
            retList.addAll(getDepartmentIdsByExcutors(retList, taxTypes));
        } else if (tAUser.hasRole(TARole.ROLE_OPER)) {
            // 1
            for (Department dep : departmentDao.getAllChildren(tAUser.getDepartmentId())) {
                retList.add(dep.getId());
            }
            // 2
            retList.addAll(getDepartmentIdsByExcutors(retList, taxTypes));
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
            if (retList.isEmpty()) {
                // Если такого подразделения (с типом 2) не найдено, то включить в выборку только подразделение пользователя.
                retList.add(departmentDao.getDepartment(tAUser.getDepartmentId()));
            }
            // подразделения с типом 3
            retList.addAll(departmentDao.getDepartmentsByType(DepartmentType.CSKO_PCP.getCode()));
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
        retList.addAll(getExecutorsDepartments(retList, formData.getFormType().getId()));
        return retList;
    }

    // http://conf.aplana.com/pages/viewpage.action?pageId=11383234
    @Override
    public List<Integer> getOpenPeriodDepartments(TAUser tAUser, List<TaxType> taxTypes, int reportPeriodId) {
        ReportPeriod reportPeriod = periodService.getReportPeriod(reportPeriodId);
        List<Integer> retList = new ArrayList<Integer>();
        // Подразделения согласно выборке 40 - Выборка для доступа к экземплярам НФ/деклараций
        List<Integer> list = getTaxFormDepartments(tAUser, taxTypes, reportPeriod.getCalendarStartDate(), reportPeriod.getEndDate());
        for (Integer departmentId : list) {
            DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodDao.get(reportPeriodId,
                    departmentId.longValue());
            if ((departmentReportPeriod != null) && departmentReportPeriod.isActive()) {
                // Подразделения, для которых открыт указанный период
                retList.add(departmentId);
            }
        }

        Set setItems = new HashSet(retList);
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
    public List<Integer> getDepartmentsByName(String departmentName) {
        try {
            return departmentDao.getDepartmentsByName(departmentName);
        } catch (DaoException e){
            throw new ServiceException("Получение списка подразделений по имени.", e);
        }
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

    private List<Integer> getExecutorsDepartments(List<Integer> departments, int formType) {
        return departmentDao.getPerformers(departments, formType);
    }

    /**
     * Все подразделения, для форм которых, подразделения departments назначены исполнителями
     */
    private List<Integer> getDepartmentIdsByExcutors(List<Integer> departments, List<TaxType> taxTypes) {
        return departmentDao.getDepartmentIdsByExcutors(departments, taxTypes);
    }
}
