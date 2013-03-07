package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.script.DeclarationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

/*
 * author auldanov
 */

@Service("declarationService")
public class DeclarationServiceImpl implements DeclarationService{

	private static final String DATE_FORMAT = "yyyyMMdd";

	@Autowired
	DeclarationDataDao declarationDataDao;
	/*
	@Autowired
	DepartmentService departmentService;

	@Autowired
	DeclarationTypeDao declarationTypeDao;
    */
	@Override
	public DeclarationData find(int declarationTypeId, int departmentId, int reportPeriodId) {
		return declarationDataDao.find(declarationTypeId, departmentId, reportPeriodId);
	}
	/*
	@Override
	public String generateXmlFileId(int declarationTypeId, int departmentId) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
		String declarationPrefix = declarationTypeDao.get(declarationTypeId).getTaxType().getDeclarationPrefix();
		DepartmentParam departmentParam = departmentService.getDepartmentParam(departmentId);
		Calendar calendar = Calendar.getInstance();
		StringBuilder stringBuilder = new StringBuilder(declarationPrefix);
		stringBuilder.append('_' +
				departmentParam.getTaxOrganCode() + '_' +
				departmentParam.getTaxOrganCode() + '_' +
				departmentParam.getInn() + departmentParam.getKpp() + '_' +
				dateFormat.format(calendar.getTime()) + '_' +
				UUID.randomUUID().toString().toUpperCase());
		return stringBuilder.toString();
	}
    */
}
