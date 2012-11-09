package com.aplana.sbrf.taxaccounting.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.DepartmentMapper;
import com.aplana.sbrf.taxaccounting.model.Department;

@Repository("departmentDao")
public class DepartmentDaoImpl implements DepartmentDao {
	private Log logger = LogFactory.getLog(getClass());

	@Autowired 
	private DepartmentMapper departmentMapper;
	
	@Override
	public Department getDepartment(int id) {
		Department result = departmentMapper.get(id);
		if (result == null) {
			logger.warn("Couldn't find department with id = " + id);
		}
		return result;
	}
}
