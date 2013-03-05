package com.aplana.sbrf.taxaccounting.service.script.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentService;

@Service("departmentService")
public class DepartmentServiceImpl implements DepartmentService {

	@Autowired
	DepartmentParamDao dao;
	
	@Override
	public DepartmentParam getDepartmentParam(int departmentId) {
		return dao.getDepartmentParam(departmentId);
	}

	@Override
	public DepartmentParamIncome getDepartmentParamIncome(int departmentId) {
		return dao.getDepartmentParamIncome(departmentId);
	}

	@Override
	public DepartmentParamTransport getDepartmentParamTransport(int departmentId) {
		return dao.getDepartmentParamTransport(departmentId);
	}
}
