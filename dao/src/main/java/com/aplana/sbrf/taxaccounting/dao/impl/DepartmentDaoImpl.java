package com.aplana.sbrf.taxaccounting.dao.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.DepartmentMapper;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;

@Repository
@Transactional(readOnly = true)
public class DepartmentDaoImpl implements DepartmentDao {
	private final Log logger = LogFactory.getLog(getClass());
	
	@Autowired 
	private DepartmentMapper departmentMapper;
	
	@Override	
	public Department getDepartment(int id) {
		if (logger.isDebugEnabled()) {
			logger.debug("Fetching department with id = " + id  + " from database");
		}
		Department result = departmentMapper.get(id);
		if (result == null) {
			throw new DaoException("Не удалось найти подразделение банка с id = " + id);
		}
		return result;
	}

	@Override
	public List<Department> getChildren(int parentDepartmentId){
		return departmentMapper.getChildren(parentDepartmentId);
	}

    @Override
    public List<Department> listDepartments(){
        return departmentMapper.getAll();
    }
}
