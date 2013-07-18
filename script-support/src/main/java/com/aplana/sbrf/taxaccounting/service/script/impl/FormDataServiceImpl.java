package com.aplana.sbrf.taxaccounting.service.script.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;


import com.aplana.sbrf.taxaccounting.service.script.FormDataService;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;

/*
 * Реализация FormDataService
 * @author auldanov
 */
@Transactional(readOnly = true)
@Component("formDataService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FormDataServiceImpl implements FormDataService, ScriptComponentContextHolder {
	
	private ScriptComponentContext scriptComponentContext;

	@Autowired
	private FormDataDao dao;
	
	@Autowired
	private DataRowHelperImpl dataRowServiceImpl;
	
	@Override
	public FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId) {
		return dao.find(formTypeId, kind, departmentId, reportPeriodId);
	}

	@Override
	public DataRowHelper getDataRowHelper(FormData formData) {
		dataRowServiceImpl.setFormData(formData);
		dataRowServiceImpl.setScriptComponentContext(scriptComponentContext);
		return dataRowServiceImpl;
	}

	@Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		this.scriptComponentContext = context;
	}
	
	
}
