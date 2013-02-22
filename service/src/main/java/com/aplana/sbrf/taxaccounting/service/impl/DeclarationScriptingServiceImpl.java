package com.aplana.sbrf.taxaccounting.service.impl;

import groovy.xml.MarkupBuilder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataCollection;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.DeclarationScriptingService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Реализация сервиса для запуска скриптов по декларациями
 * @author dsultanbekov
 */
@Service
public class DeclarationScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements DeclarationScriptingService {
	@Autowired
	private DepartmentFormTypeDao departmentFormTypeDao;
	
	@Autowired
	private FormDataDao formDataDao;
	
	@Autowired
	private DepartmentDao departmentDao;
	
	@Autowired
	private FormTypeDao formTypeDao;
	
	@Autowired
	DeclarationTemplateDao declarationTemplateDao;
	
	@Override
	public String create(Logger logger, int departmentId, int declarationTemplateId, int reportPeriodId) {
		this.logger.debug("Starting processing request to run create script");
		
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);
		List<DepartmentFormType> sourcesInfo = departmentFormTypeDao.getDeclarationSources(departmentId, declarationTemplate.getDeclarationType().getId());
		List<FormData> records = new ArrayList<FormData>();
		Iterator<DepartmentFormType> i = sourcesInfo.iterator(); 
		while(i.hasNext()) {
			DepartmentFormType dft = i.next();
			// В будущем возможны ситуации, когда по заданному сочетанию параметров будет несколько
			// FormData, в этом случае данный код нужно будет зарефакторить
			FormData fd = formDataDao.find(dft.getFormTypeId(), dft.getKind(), dft.getDepartmentId(), reportPeriodId);
			if (fd != null) {
				if (fd.getState() != WorkflowState.ACCEPTED) {
					Department department = departmentDao.getDepartment(dft.getDepartmentId());
					FormType formType = formTypeDao.getType(dft.getFormTypeId());
					logger.warn(
						"Форма-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", тип формы: \"%s\", подразделение: \"%s\"",
						formType.getName(),
						dft.getKind().getName(),
						department.getName()
					);
				} else {
					records.add(fd);				
					i.remove();
				}
			} else {
				Department department = departmentDao.getDepartment(dft.getDepartmentId());
				FormType formType = formTypeDao.getType(dft.getFormTypeId());
				logger.warn(
					"Не хватает источника данных, вид формы: \"%s\", тип формы: \"%s\", подразделение: \"%s\"",
					formType.getName(),
					dft.getKind().getName(),
					department.getName()
				);
			}
		}
		
		FormDataCollection formDataCollection = new FormDataCollection();
		formDataCollection.setRecords(records);
		
		Bindings b = scriptEngine.createBindings();
		b.putAll(getScriptExposedBeans(declarationTemplate.getDeclarationType().getTaxType()));
		b.put("formDataCollection", formDataCollection);
		
		b.put("departmentId", departmentId);
				
		StringWriter writer = new StringWriter();
		MarkupBuilder xml = new MarkupBuilder(writer);
		b.put("xml", xml);
		try {
			scriptEngine.eval(declarationTemplate.getCreateScript(), b);
		} catch (ScriptException e) {
			logScriptException(e, logger);
		} catch (Exception e) {
			logger.error(e);
		}
		
		if (logger.containsLevel(LogLevel.ERROR)) {
			return null;
		} else {
			logger.info("Декларация успешно создана");
			return writer.toString();
		}
		
	}
	
	/**
	 * Возвращает спринг-бины доcтупные для использования в скрипте создания декларации.
	 * @param taxType вид налога
	 */
	private Map<String, ?> getScriptExposedBeans(TaxType taxType){
		Map<String, Object> beans = new HashMap<String, Object>();

		for(Map.Entry<String, ?> entry:applicationContext.getBeansWithAnnotation(ScriptExposed.class).entrySet()){
			Object bean = entry.getValue();
			ScriptExposed scriptExposed = AnnotationUtils.findAnnotation(bean.getClass(), ScriptExposed.class);
			boolean flag = true;

			if(scriptExposed.taxTypes().length>0){
				flag = false;

				for(TaxType tt: scriptExposed.taxTypes()){
					if(taxType == tt){
						flag = true;
						break;
					}
				}
			}
			if(flag){
				beans.put(entry.getKey(), bean);
			}
		}
		return beans;
	}
	
}
