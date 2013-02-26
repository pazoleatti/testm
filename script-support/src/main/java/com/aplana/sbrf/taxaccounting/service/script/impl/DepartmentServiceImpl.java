package com.aplana.sbrf.taxaccounting.service.script.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentService;
import org.springframework.stereotype.Service;


@Service
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	DepartmentParamDao dao;
	
	@Override
	public DepartmentParam getDepartmentParam(int departmentId) {
		return dao.getDepartmentParam(departmentId);
	}
}
