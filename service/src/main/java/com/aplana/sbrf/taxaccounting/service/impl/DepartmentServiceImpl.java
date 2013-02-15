package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;

@Service("departmentService")
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
		
	@Autowired
	DepartmentDao departmentDao;

	@Override
	public Department getDepartment(int departmentId) {
		Department department = departmentDao.getDepartment(departmentId);
		return department;
	}

	@Override
	public List<Department> getChildren(int parentDepartmentId) {
		return departmentDao.getChildren(parentDepartmentId);
	}

}
