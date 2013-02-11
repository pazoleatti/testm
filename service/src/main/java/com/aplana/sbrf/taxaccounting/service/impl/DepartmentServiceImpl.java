package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;

@Service("departmentService")
@Transactional
public class DepartmentServiceImpl implements DepartmentService {
	
	@Autowired
	DepartmentDao departmentDao;
	
	@Autowired
	DepartmentFormTypeDao departmentFormTypeDao;
	
	@Autowired
	DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

	@Override
	@Cacheable("Department")
	public Department getDepartment(int departmentId) {
		Department department = departmentDao.getDepartment(departmentId);
		department.setDepartmentFormTypes(departmentFormTypeDao.get(departmentId));
		department.setDepartmentDeclarationTypes(departmentDeclarationTypeDao.getDepartmentDeclarationTypes(departmentId));
		return department;
	}

	@Override
	public List<Department> getChildrensDepartment(int parentDepartmentId) {
		return departmentDao.getChildren(parentDepartmentId);
	}

}
