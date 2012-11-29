package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.mapper.DepartmentMapper;
import com.aplana.sbrf.taxaccounting.model.Department;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository("departmentDao")
@Transactional(readOnly = true)
public class DepartmentDaoImpl implements DepartmentDao {
	@Autowired 
	private DepartmentMapper departmentMapper;
	
	@Override
	public Department getDepartment(int id) {
		Department result = departmentMapper.get(id);
		if (result == null) {
			throw new DaoException("Не удалось найти подразделение банка с id = " + id);
		}
		return result;
	}

	public List<Department> getChildren(int parentDepartmentId){
		return departmentMapper.getChildren(parentDepartmentId);
	}

    @Override
    public List<Department> listDepartments(){
        return departmentMapper.getAll();
    }
}
