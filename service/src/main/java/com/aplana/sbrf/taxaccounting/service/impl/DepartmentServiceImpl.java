package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
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
	public List<Department> listAll(){
		return departmentDao.listDepartments();
	}

	@Override
	public List<Department> getChildren(int parentDepartmentId) {
		return departmentDao.getChildren(parentDepartmentId);
	}

	@Override
	public Department getParent(int departmentId){
		return departmentDao.getParent(departmentId);
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


	@Override
 	public Set<Department> getRequiredForTreeDepartments(Set<Integer> availableDepartments){
		Set<Department> departmentSet = new HashSet<Department>();
		for(Integer departmentId : availableDepartments){
			departmentSet.add(getDepartment(departmentId));
		}
		for(Integer departmentId : availableDepartments){
			Integer searchFor = departmentId;
			while (true){
				Department department = getParent(searchFor);
				if(department == null){
					break;
				}
				if(department.getParentId() == null || departmentSet.contains(department.getParentId())){
					departmentSet.add(department);
					break;
				} else {
					departmentSet.add(department);
					searchFor = department.getParentId();
				}
			}
		}
		return departmentSet;
	}

}
