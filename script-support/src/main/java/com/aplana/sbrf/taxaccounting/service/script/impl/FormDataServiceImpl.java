package com.aplana.sbrf.taxaccounting.service.script.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.service.script.FormDataService;

/*
 * Реализация FormDataService
 * @author auldanov
 */
@Repository("FormDataService")
@Transactional(readOnly = true)
public class FormDataServiceImpl extends AbstractDao implements FormDataService{

	@Autowired
	private FormDataDao dao;
	
	@Override
	public FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId) {
		return dao.find(formTypeId, kind, departmentId, reportPeriodId);
	}
}
