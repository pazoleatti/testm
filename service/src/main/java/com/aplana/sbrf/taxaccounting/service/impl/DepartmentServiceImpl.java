package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.google.common.collect.Lists;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentDao departmentDao;

    public DepartmentServiceImpl(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

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
        Map<Integer, Department> departmentMap = new HashMap<>();

        List<Department> departmentList = availableDepartments == null ? this.listAll() :
                departmentDao.getRequiredForTreeDepartments(new ArrayList<>(availableDepartments));

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
            return new ArrayList<>(0);
        return departmentDao.findAllChildrenIdsById(depId);
    }

    @Override
    public List<Department> getBADepartments(TAUser tAUser, TaxType taxType) {
        List<Department> retList = new ArrayList<>();

        if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmentTBChildren(tAUser.getDepartmentId()));
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_OPER)) {
            retList.addAll(departmentDao.getAllChildren(tAUser.getDepartmentId()));
        }

        return retList;
    }

    @Override
    public List<Integer> getBADepartmentIds(TAUser tAUser) {
        List<Integer> retList = new ArrayList<>();

        if (tAUser.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.fetchAllIds());
        } else if (tAUser.hasRole(TARole.N_ROLE_CONTROL_NS)) {
            retList.addAll(departmentDao.getDepartmentTBChildrenId(tAUser.getDepartmentId()));
        } else if (tAUser.hasRole(TARole.N_ROLE_OPER)) {
            retList.addAll(departmentDao.findAllChildrenIdsById(tAUser.getDepartmentId()));
        }

        return retList;
    }

    @Override
    public List<Integer> getTBDepartmentIds(TAUser tAUser, TaxType taxType, boolean addRoot) {
        List<Integer> retList = new ArrayList<>();

        if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_UNP)) {
            if (addRoot) {
                // подразделение с типом 1
                retList.addAll(departmentDao.getDepartmentIdsByType(DepartmentType.ROOT_BANK.getCode()));
            }
            // подразделения с типом 2
            retList.addAll(departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode()));
        } else if (tAUser.hasRole(taxType, TARole.N_ROLE_CONTROL_NS)) {
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
    @Cacheable(value = CacheConstants.DEPARTMENT, key = "'user_departments_'+#user.id")
    public List<Integer> findAllAvailableIds(TAUser user) {
        if (user.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
            // Все подразделения из справочника подразделений
            return departmentDao.fetchAllIds();
        } else if (user.hasRole(TARole.N_ROLE_CONTROL_NS)) {
            Set<Integer> tbDepartmentIds = new HashSet<>();
            // ТБ подразделения пользователя
            tbDepartmentIds.add(departmentDao.getParentTBId(user.getDepartmentId()));
            // ТБ, на которые (или на их дочерние) подразделение пользователя назначено исполнителем
            tbDepartmentIds.addAll(departmentDao.findAllTBIdsByPerformerId(user.getDepartmentId()));
            // В итоге возвращяем все ТБ выше и все их дочерние
            return departmentDao.findAllChildrenIdsByIds(tbDepartmentIds);
        } else if (user.hasRole(TARole.N_ROLE_OPER)) {
            Set<Integer> departmentIds = new HashSet<>();
            // подразделение пользователя
            departmentIds.add(user.getDepartmentId());
            // подразделения, на которые подразделение пользователя назначено исполнителем
            departmentIds.addAll(departmentDao.findAllIdsByPerformerIds(Lists.newArrayList((Integer) user.getDepartmentId())));
            // В итоге возвращяем все подразделение выше и все их дочерние
            return departmentDao.findAllChildrenIdsByIds(departmentIds);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Integer> findAllAvailableTBIds(TAUser user) {
        if (user.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
            return departmentDao.getDepartmentIdsByType(DepartmentType.TERR_BANK.getCode());
        } else {
            Integer userTBId = departmentDao.getParentTBId(user.getDepartmentId());
            // Все ТБ, для которых подразделение пользователя назначено исполнителем.
            List<Integer> TBIds = departmentDao.findAllTBIdsByPerformerId(user.getDepartmentId());
            if (!TBIds.contains(userTBId)) {
                TBIds.add(userTBId);
            }
            return TBIds;
        }
    }

    @Override
    public List<Department> getDestinationDepartments(TaxType taxType, TAUser tAUser) {
        List<Department> retList = new ArrayList<>();
        if (tAUser.hasRoles(TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS)) {
            // все подразделения из справочника подразделений
            retList.addAll(departmentDao.listDepartments());
        }

        // Результат выборки должен содержать только уникальные подразделения
        Set<Department> setItems = new HashSet<>(retList);
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
    public Map<Integer, Department> getDepartments(List<Integer> departmentId) {
        Map<Integer, Department> result = new HashMap<>();
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
    public Integer getParentTBId(int departmentId) {
        return departmentDao.getParentTBId(departmentId);
    }

    @Override
    public List<Department> findAllByIdIn(List<Integer> ids) {
        return departmentDao.findAllByIdIn(ids);
    }

    @Override
    public PagingResult<DepartmentName> searchDepartmentNames(String name, PagingParams pagingParams) {
        return departmentDao.searchDepartmentNames(name, pagingParams);
    }

    @Override
    public PagingResult<DepartmentShortInfo> fetchAllTBShortInfo(String filter, PagingParams pagingParams) {
        return departmentDao.fetchAllTBShortInfo(filter, pagingParams);
    }
}
