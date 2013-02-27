package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * author auldanov
 */

@Service("declarationService")
public class DeclarationServiceImpl implements DeclarationService{

	@Autowired
	DeclarationDataDao dao;
	
	@Override
	public DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId) {
		return dao.find(declarationTypeId, departmentId, reportPeriodId);
	}

}
