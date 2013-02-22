package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.List;

import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;

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

	@Autowired
	DepartmentParamDao departmentParamDao;

	@Override
	public Department getDepartment(int departmentId) {
		Department department = departmentDao.getDepartment(departmentId);
		return department;
	}

	@Override
	public List<Department> getChildren(int parentDepartmentId) {
		return departmentDao.getChildren(parentDepartmentId);
	}

	@Override
	public DepartmentParam getDepartmentParam(int departmentId) {
		return departmentParamDao.getDepartmentParam(departmentId);
	}

	@Override
	public List<Department> getIsolatedDepartments() {
		return departmentDao.getIsolatedDepartments();
	}

	@Override
	public DepartmentParamIncome getDepartmentParamIncome(int departmentId) {
		return departmentParamDao.getDepartmentParamIncome(departmentId);
	}

	@Override
	public DepartmentParamTransport getDepartmentParamTransport(int departmentId) {
		return departmentParamDao.getDepartmentParamTransport(departmentId);
	}

}
