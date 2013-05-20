package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.log.impl.RowScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Реализация сервиса для выполнения скриптов над формой.
 */
@Service
public class FormDataScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements ApplicationContextAware, FormDataScriptingService {

	private static final String DUPLICATING_ARGUMENTS_ERROR = "The key \"%s\" already exists in map. Can't override of them.";

	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private ReportPeriodDao reportPeriodDao;

	public FormDataScriptingServiceImpl() {
	}
	
	@Override
	public void executeScript(TAUser user, FormData formData,
			FormDataEvent event, Logger logger,
			Map<String, Object> additionalParameters) {
		
		// Если отчетный период для ввода остатков, то не выполняем скрипты
		if (reportPeriodDao.get(formData.getReportPeriodId()).isBalancePeriod()) {
			return;
		}
		
		// Если скрипт отсутствует, то ничего не делаем
		String script = formTemplateDao.get(formData.getFormTemplateId()).getScript();
		if (script == null || script.trim().isEmpty()) {
			// TODO: Semyon Goryachkin: Нужно будет удалить вызов старого метода, 
			//       06.05.2013         когда миграция скриптов будет закончена 
			//                          (SBRFACCTAX-2233, )
			executeScripts(user, formData, event, logger, additionalParameters);
			//
			return;
		}
		
		// Биндим параметры для выполнения скрипта
		Bindings b = scriptEngine.createBindings();
		b.putAll(getScriptExposedBeans(formData.getFormType().getTaxType(), event));		
		b.put("formDataEvent", event);
		b.put("logger", logger);
		b.put("formData", formData);
		if (user != null) {
			b.put("user", user);
			b.put("userDepartment", departmentService.getDepartment(user.getDepartmentId()));
		}
		b.put("formDataDepartment", departmentService.getDepartment(formData.getDepartmentId()));
		if (additionalParameters != null) {
			for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
				if (b.containsKey(entry.getKey()))
					throw new IllegalArgumentException(String.format(DUPLICATING_ARGUMENTS_ERROR, entry.getKey()));
				b.put(entry.getKey(), entry.getValue());
			}
		}

		try {
			ScriptMessageDecorator d = new ScriptMessageDecorator(event.getTitle());
			logger.setMessageDecorator(d);
			
			executeScript(b, script, logger, d);
			
			logger.setMessageDecorator(null);
		} finally {
			b.remove("logger");
			b.remove("formData");
			b.remove("user");
			b.remove("userDepartment");
			b.remove("formDataDepartment");
			if (additionalParameters != null) {
				for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
					b.remove(entry.getKey());
				}
			}
		}
	}

	@Deprecated
	private void executeScripts(TAUser user, FormData formData, FormDataEvent event, Logger logger, Map<String, Object> additionalParameters) {
		// Если отчетный период для ввода остатков, то не выполняем скрипты
		if (reportPeriodDao.get(formData.getReportPeriodId()).isBalancePeriod()) {
			return;
		}
		Bindings b = scriptEngine.createBindings();
		b.putAll(getScriptExposedBeans(formData.getFormType().getTaxType(), event));

		// predefined script variables
		b.put("formDataEvent", event);
		b.put("logger", logger);
		b.put("formData", formData);
		if (user != null) {
			b.put("user", user);
			b.put("userDepartment", departmentService.getDepartment(user.getDepartmentId()));
		}
		b.put("formDataDepartment", departmentService.getDepartment(formData.getDepartmentId()));
		if (additionalParameters != null) {
			for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
				if (b.containsKey(entry.getKey()))
					throw new IllegalArgumentException(String.format(DUPLICATING_ARGUMENTS_ERROR, entry.getKey()));
				b.put(entry.getKey(), entry.getValue());
			}
		}

		try {
			// execute scripts
			List<Script> scripts = getScriptsByEvent(formData, event);
			for (Script script : scripts) {
				if (script.isRowScript()) {
					executeRowScript(b, script, formData, logger);
				} else {
					executeFormScript(b, script, logger);
				}
			}
		} finally {
			b.remove("logger");
			b.remove("formData");
			b.remove("user");
			b.remove("userDepartment");
			b.remove("formDataDepartment");
			if (additionalParameters != null) {
				for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
					b.remove(entry.getKey());
				}
			}
		}
	}

	/**
	 * Возвращает спринг-бины доcтупные для использования в скрипте.
	 *
	 * @param taxType тип налога
	 * @param event событие
	 */
	private Map<String, ?> getScriptExposedBeans(TaxType taxType, FormDataEvent event){
		Map<String, Object> beans = new HashMap<String, Object>();

		for(Map.Entry<String, ?> entry:applicationContext.getBeansWithAnnotation(ScriptExposed.class).entrySet()){
			Object bean = entry.getValue();
			ScriptExposed scriptExposed = AnnotationUtils.findAnnotation(bean.getClass(), ScriptExposed.class);
			boolean flag = true;

			if(scriptExposed.taxTypes().length>0){
				boolean has = false;

				for(TaxType tt: scriptExposed.taxTypes()){
					if(taxType == tt){
						has = true;
					}
				}

				if(!has){
					flag = false;
				}
			}

			if(scriptExposed.formDataEvents().length>0){
				boolean has = false;

				for(FormDataEvent ev:scriptExposed.formDataEvents()){
					if(ev == event){
						has = true;
					}
				}

				if(!has){
					flag = false;
				}
			}

			if(flag){
				beans.put(entry.getKey(), bean);
			}
		}

		return beans;
	}

	/**
	 * Проверяет, есть ли скрипты для события формы
	 * TODO: Semyon Goryachkin: Нужно будет удалить метод, 
	         06.05.2013         когда миграция скриптов будет закончена 
		                        (SBRFACCTAX-2233, )
	 *
	 * @param formData форма
	 * @param event    событие
	 */
	@Override
	@Deprecated
	public boolean hasScripts(FormData formData, FormDataEvent event) {
		// Если скрипт отсутствует, то ничего не делаем
		String script = formTemplateDao.get(formData.getFormTemplateId()).getScript();
		if (script == null || script.trim().isEmpty()) {

			return !getScriptsByEvent(formData, event).isEmpty();
			//
		} else {
			return true;
		}
		
	}

	@Deprecated
	private List<Script> getScriptsByEvent(FormData formData, FormDataEvent event) {
		return formTemplateDao.get(formData.getFormTemplateId()).getScriptsByEvent(event);
	}

	/**
	 * Выполняет скрипт для всей формы в целом.
	 *
	 * @param bindings переменные окружения скрипта
	 * @param script скрипт формы
	 * @param logger логгер
	 */
	@Deprecated
	private void executeFormScript(Bindings bindings, Script script, Logger logger) {
		ScriptMessageDecorator d = new ScriptMessageDecorator();
		logger.setMessageDecorator(d);

		executeScriptOld(bindings, script, logger, d);

		logger.setMessageDecorator(null);
	}

	/**
	 * Выполняет скрипт для каждой строки формы.
	 *
	 * @param bindings переменные окружения скрипта
	 * @param script   скрипт строки
	 * @param formData данные формы
	 * @param logger   логгер
	 */
	@Deprecated
	private void executeRowScript(Bindings bindings, Script script, FormData formData, Logger logger) {
		RowScriptMessageDecorator decorator = new RowScriptMessageDecorator();
		logger.setMessageDecorator(decorator);

		// Если происходит ошибка, то выполнение данного скрипта для оставшихся строк останавливается.
		// Если мы продолжим выполнение скрипта, то рискуем получить количество ошибок по количеству строк в форме,
		// т.е. очень много. С другой стороны, если мы останавливаем выполнение, то не получим все ошибки и
		// лишим пользователя возможности исправить все ошибки сразу.
		//
		// TODO: Надо подумать над поведением.
		boolean error = false;
		for (ListIterator<DataRow<Cell>> i = formData.getDataRows().listIterator(); i.hasNext() && !error; ) {
			int rowIndex = i.nextIndex();
			DataRow row = i.next();

			decorator.setRowIndex(rowIndex + 1); // Для пользователя нумерация строк должна начинаться с 1

			bindings.put("row", row);
			bindings.put("rowIndex", rowIndex);
			bindings.put("rowAlias", row.getAlias());

			error = !executeScriptOld(bindings, script, logger, decorator);

			bindings.remove("row");
			bindings.remove("rowIndex");
			bindings.remove("rowAlias");
		}

		logger.setMessageDecorator(null);
	}

	/**
	 * Выполняет любой скрипт, ловит ошибки, записывает их в логгер.
	 *
	 * @param bindings переменные окружения скрипта
	 * @param script    скрипт
	 * @param logger    логгер
	 * @param decorator декоратор для ошибок
	 */
	@Deprecated
	private boolean executeScriptOld(Bindings bindings, Script script, Logger logger, ScriptMessageDecorator decorator) {
		try {
			boolean executeCondition = true;

			// Condition
			if (script.getCondition() != null) {
				decorator.setScriptName(script.getName() + " - Условие исполнения");
				executeCondition = (Boolean) scriptEngine.eval(script.getCondition(), bindings);
			}

			// If condition is absent or is true then execute script
			if (executeCondition) {
				decorator.setScriptName(script.getName());
				scriptEngine.eval(script.getBody(), bindings);
			}

			return true;
		} catch (ScriptException e) {
			logScriptException(e, logger);
			return false;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	
	
	private boolean executeScript(Bindings bindings, String script, Logger logger, ScriptMessageDecorator decorator) {
		try {
			scriptEngine.eval(script, bindings);
			return true;
		} catch (ScriptException e) {
			logScriptException(e, logger);
			return false;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}
	


}
