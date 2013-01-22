package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.log.impl.RowScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.Script;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Реализация сервиса для выполнения скриптов над формой.
 */
@Service
public class FormDataScriptingServiceImpl implements ApplicationContextAware, FormDataScriptingService {
	/**
	 * Предопределенные пакеты для импорта в скрипты. Здесь только пакеты.
	 */
	private static final String[] PREDEFINED_IMPORTS = new String[]{
			"com.aplana.sbrf.taxaccounting.model",
			"com.aplana.sbrf.taxaccounting.model.dictionary",
			"com.aplana.sbrf.taxaccounting.model.log",
			"com.aplana.sbrf.taxaccounting.model.security",
			"com.aplana.sbrf.taxaccounting.dao.exсeption"
	};
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private DepartmentDao departmentDao;

	private ApplicationContext applicationContext;
	
	private ScriptEngine scriptEngine;

	public FormDataScriptingServiceImpl() {
		ScriptEngineManager factory = new ScriptEngineManager();
		this.scriptEngine = factory.getEngineByName("groovy");

		// Predefined imports
		CompilerConfiguration config = new CompilerConfiguration();
		ImportCustomizer ic = new ImportCustomizer();
		ic.addStarImports(PREDEFINED_IMPORTS);
		config.addCompilationCustomizers(ic);

		GroovyScriptEngineImpl groovyScriptEngine = (GroovyScriptEngineImpl) this.scriptEngine;
		GroovyClassLoader classLoader = groovyScriptEngine.getClassLoader();
		classLoader = new GroovyClassLoader(classLoader, config, false);
		groovyScriptEngine.setClassLoader(classLoader);
	}
	
	/**
	 * Выполняет скрипты формы по определенному событию.
	 *
	 * @param user     текущий пользователь. Вообще, сомнительно его здесь нахождение. Моё мнение: выполднение скриптов
	 *                 не должно зависеть от пользователя.
	 * @param formData данные налоговой формы
	 * @param event    событие формы
	 * @param logger   логгер для сохранения ошибок выполнения скриптов.
	 */
	@Override
	public void executeScripts(TAUser user, FormData formData, FormDataEvent event, Logger logger) {

		Bindings b = scriptEngine.createBindings();
		b.putAll(getScriptExposedBeans(formData.getFormType().getTaxType(), event));
		
		// predefined script variables
		b.put("logger", logger);
		b.put("formData", formData);
		if (user != null) {
			b.put("user", user);
			b.put("userDepartment", departmentDao.getDepartment(user.getDepartmentId()));
		}
		b.put("formDataDepartment", departmentDao.getDepartment(formData.getDepartmentId()));

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
		}
	}

	/**
	 * Возвращает спринг-бины доятупные для использования в скрипте.
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
	 *
	 * @param formData форма
	 * @param event    событие
	 */
	@Override
	public boolean hasScripts(FormData formData, FormDataEvent event) {
		return !getScriptsByEvent(formData, event).isEmpty();
	}

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
	private void executeFormScript(Bindings bindings, Script script, Logger logger) {
		ScriptMessageDecorator d = new ScriptMessageDecorator();
		logger.setMessageDecorator(d);

		executeScript(bindings, script, logger, d);

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
		for (ListIterator<DataRow> i = formData.getDataRows().listIterator(); i.hasNext() && !error; ) {
			int rowIndex = i.nextIndex();
			DataRow row = i.next();

			decorator.setRowIndex(rowIndex + 1); // Для пользователя нумерация строк должна начинаться с 1
			
			bindings.put("row", row);
			bindings.put("rowIndex", rowIndex);
			bindings.put("rowAlias", row.getAlias());

			error = !executeScript(bindings, script, logger, decorator);

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
	private boolean executeScript(Bindings bindings, Script script, Logger logger, ScriptMessageDecorator decorator) {
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
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof Exception) {
				logger.error((Exception) cause);
			} else {
				logger.error(e);
			}
			return false;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.applicationContext = context;
	}
}
