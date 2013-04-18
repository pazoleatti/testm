package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import groovy.xml.MarkupBuilder;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Реализация сервиса для запуска скриптов по декларациями
 * @author dsultanbekov
 * @author mfayzullin
 */
@Service
public class DeclarationDataScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements DeclarationDataScriptingService {

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

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"windows-1251\"?>";
	
	@Override
	public String create(Logger logger, DeclarationData declarationData, String docDate) {
		this.logger.debug("Starting processing request to run create script");
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());

		// Формирование списка НФ-источников в статусе "Принята"
		FormDataCollection formDataCollection = getAcceptedFormDataSources(logger, declarationData);
		// Должна быть хотя бы одна НФ-источник
		if (formDataCollection.getRecords().isEmpty()) {
			logger.error("Декларация не может быть сформирована, так как по крайней мере одна сводная или выходная " +
					"налоговая форма, необходимая для формирования декларации, должна иметь статус «Принята».");
		} else {
			Bindings b = scriptEngine.createBindings();
			b.putAll(getScriptExposedBeans(declarationTemplate.getDeclarationType().getTaxType()));
			b.put("formDataCollection", formDataCollection);
			b.put("declarationData", declarationData);
			b.put("docDate", docDate);
			StringWriter writer = new StringWriter();
			MarkupBuilder xml = new MarkupBuilder(writer);
			b.put("xml", xml);
			b.put("logger", logger);
			try {
				scriptEngine.eval(declarationTemplate.getCreateScript(), b);
			} catch (ScriptException e) {
				logScriptException(e, logger);
			} catch (Exception e) {
				logger.error(e);
			}

			if (!logger.containsLevel(LogLevel.ERROR)) {
				logger.info("Декларация успешно создана");
				return XML_HEADER.concat(writer.toString());
			}
		}
		return null;
	}

	@Override
	public void accept(Logger logger, DeclarationData declarationData) {
		int declarationTemplateId = declarationData.getDeclarationTemplateId();
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);

		Bindings b = scriptEngine.createBindings();
		b.putAll(getScriptExposedBeans(declarationTemplate.getDeclarationType().getTaxType()));
		b.put("formDataCollection", getAcceptedFormDataSources(logger, declarationData));
		b.put("declarationData", declarationData);
		b.put("xml", null);
		b.put("logger", logger);
		try {
			scriptEngine.eval(declarationTemplate.getCreateScript(), b);
		} catch (ScriptException e) {
			logScriptException(e, logger);
		} catch (Exception e) {
			logger.error(e);
		}

		if (!logger.containsLevel(LogLevel.ERROR)) {
			logger.info("Декларация успешно принята!");
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

	/**
	 * Возвращает список налоговых форм, являющихся источником для указанной декларации и находящихся в статусе
	 * "Создана"
	 *
	 * @param logger журнал сообщений
	 * @param declarationData декларация
	 * @return список НФ-источников в статусе "Принята"
	 */
	private FormDataCollection getAcceptedFormDataSources(Logger logger, DeclarationData declarationData) {
		int departmentId = declarationData.getDepartmentId();
		int declarationTemplateId = declarationData.getDeclarationTemplateId();
		int reportPeriodId = declarationData.getReportPeriodId();

		// Формирование списка НФ-источников в статусе "Принята"
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationTemplateId);
		List<DepartmentFormType> sourcesInfo = departmentFormTypeDao.getDeclarationSources(departmentId, declarationTemplate.getDeclarationType().getId());
		List<FormData> records = new ArrayList<FormData>();
		for (DepartmentFormType dft : sourcesInfo) {
			// В будущем возможны ситуации, когда по заданному сочетанию параметров будет несколько
			// FormData, в этом случае данный код нужно будет зарефакторить
			FormData formData = formDataDao.find(dft.getFormTypeId(), dft.getKind(), dft.getDepartmentId(), reportPeriodId);
			if (formData != null) {
				if (formData.getState() != WorkflowState.ACCEPTED) {
					Department department = departmentDao.getDepartment(dft.getDepartmentId());
					FormType formType = formTypeDao.getType(dft.getFormTypeId());
					logger.warn(
							"Форма-источник существует, но не может быть использована, так как еще не принята. Вид формы: \"%s\", тип формы: \"%s\", подразделение: \"%s\"",
							formType.getName(),
							dft.getKind().getName(),
							department.getName()
					);
				} else {
					records.add(formData);
				}
			}
		}
		FormDataCollection formDataCollection = new FormDataCollection();
		formDataCollection.setRecords(records);
		return formDataCollection;
	}

}
