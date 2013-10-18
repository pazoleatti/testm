package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.script.FormDataCacheDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.script.FormDataService;
import com.aplana.sbrf.taxaccounting.service.script.api.DataRowHelper;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/*
 * Реализация FormDataService
 * @author auldanov
 */
@Transactional(readOnly = true)
@Component("formDataService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FormDataServiceImpl implements FormDataService, ScriptComponentContextHolder, ApplicationContextAware {
	
	private ScriptComponentContext scriptComponentContext;

	@Autowired
	private FormDataDao dao;

    @Autowired
    private FormDataCacheDao cacheDao;

    private HashMap<Number,DataRowHelper> helperHashMap = new HashMap<Number, DataRowHelper>();

    private static ApplicationContext applicationContext;
	
	@Override
	public FormData find(int formTypeId, FormDataKind kind, int departmentId, int reportPeriodId) {
		return dao.find(formTypeId, kind, departmentId, reportPeriodId);
	}

	@Override
	public DataRowHelper getDataRowHelper(FormData formData) {
        if(formData.getId() == null){
            throw new ServiceException("FormData не сохранена, id = null.");
        }
        if(helperHashMap.containsKey(formData.getId())){
            return helperHashMap.get(formData.getId());
        }
        DataRowHelperImpl dataRowHelperImpl = applicationContext.getBean(DataRowHelperImpl.class);
        dataRowHelperImpl.setFormData(formData);
        dataRowHelperImpl.setScriptComponentContext(scriptComponentContext);
        helperHashMap.put(formData.getId(), dataRowHelperImpl);
		return dataRowHelperImpl;
	}

    @Override
	public void setScriptComponentContext(ScriptComponentContext context) {
		this.scriptComponentContext = context;
	}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void fillRefBookCache(Long formDataId, Map<Long, Map<String, RefBookValue>> refBookCache) {
        if (formDataId == null || refBookCache == null) {
            return;
        }
        refBookCache.putAll(cacheDao.getRefBookMap(formDataId));
    }
}
