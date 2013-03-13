package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentService;

import java.util.List;

@Service("departmentService")
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	DepartmentParamDao departmentParamDao;
	
	@Autowired
	DepartmentDao departmentDao;
	
	@Override
	public DepartmentParam getDepartmentParam(int departmentId) {
		return departmentParamDao.getDepartmentParam(departmentId);
	}

	@Override
	public DepartmentParamIncome getDepartmentParamIncome(int departmentId) {
		return departmentParamDao.getDepartmentParamIncome(departmentId);
	}

	@Override
	public DepartmentParamTransport getDepartmentParamTransport(int departmentId) {
		return departmentParamDao.getDepartmentParamTransport(departmentId);
	}

	@Override
	public Boolean issetSbrfCode(String sbrfCode) {
		return departmentDao.getDepartmentBySbrfCode(sbrfCode) != null;
	}
	
	@Override
	public Boolean issetName(String name) {
		return departmentDao.getDepartmentByName(name) != null;
	}

    @Override
    public Department get(String name) throws IllegalArgumentException {
        List<Department> departaments = departmentDao.listDepartments();
        for (Department department : departaments) {
            if (department != null && department.getName() != null && department.getName().equals(name)) {
                return department;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Department get(Integer id) throws IllegalArgumentException {
        List<Department> departments = departmentDao.listDepartments();
        for(Department department : departments) {
            if (department != null && id.equals(department.getId())) {
                return department;
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Department getTB(String tbIndex) throws IllegalArgumentException {
        List<Department> departments = departmentDao.listDepartments();
        for(Department department : departments) {
            if (department != null && tbIndex.equals(department.getTbIndex()) && department.getType() == DepartmentType.TERBANK) {
                return department;
            }
        }
        throw new IllegalArgumentException();
    }
}

