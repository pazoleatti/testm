package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("departmentService")
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	DepartmentDao departmentDao;

    @Autowired(required = false)
    com.aplana.sbrf.taxaccounting.service.DepartmentService departmentService;

    @Override
    public Department get(Integer id) {
        List<Department> departments = departmentDao.listDepartments();
        for(Department department : departments) {
            if (department != null && id.equals(department.getId())) {
                return department;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public String getParentsHierarchy(Integer departmentId) {
        return departmentDao.getParentsHierarchy(departmentId);
    }

    @Override
    public List<Department> getAllChildren(int parentDepartmentId) {
        return departmentDao.getAllChildren(parentDepartmentId);
    }

    @Override
    public Integer getParentTBId(int departmentId) {
        return departmentDao.getParentTBId(departmentId);
    }

    @Override
    public Department getBankDepartment() {
        return departmentService.getBankDepartment();
    }

    @Override
    public List<Integer> getDepartmentIdsByType(int type) {
        return departmentDao.getDepartmentIdsByType(type);
    }

    @Override
    public Department getDepartmentBySbrfCode(String sbrfCode, boolean activeOnly) {
        return departmentService.getDepartmentBySbrfCode(sbrfCode, activeOnly);
    }

    @Override
    public Map<Integer, Department> getDepartments(List<Integer> departmentIds) {
        return departmentService.getDepartments(departmentIds);
    }
}

