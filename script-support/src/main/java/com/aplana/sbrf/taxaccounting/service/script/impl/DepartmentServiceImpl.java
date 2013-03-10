package com.aplana.sbrf.taxaccounting.service.script.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentService;

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
}

