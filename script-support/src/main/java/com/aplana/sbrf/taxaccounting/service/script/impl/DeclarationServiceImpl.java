package com.aplana.sbrf.taxaccounting.service.script.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDao;
import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import org.springframework.stereotype.Service;

/*
 * author auldanov
 */

@Service
public class DeclarationServiceImpl implements DeclarationService{

	@Autowired
	DeclarationDao dao;
	
	@Override
	public Declaration find(int declarationTypeId, int departmentId, int reportPeriodId) {
		return dao.find(declarationTypeId, departmentId, reportPeriodId);
	}

}
