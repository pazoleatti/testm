package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;
import groovy.lang.GroovyClassLoader;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.log.impl.RowScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

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
			"com.aplana.sbrf.taxaccounting.model.security"
	};
	@Autowired
	private FormTemplateDao formTemplateDao;
	@Autowired
	private FormDataDao formDataDao;
	@Autowired
	private DepartmentDao departmentDao;

	private Map<String, Object> scriptExposedBeans;

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
		ScriptEngine engine = getScriptEngine();

		// predefined script variables
		engine.put("logger", logger);
		engine.put("formData", formData);
		engine.put("user", user);
		engine.put("userDepartment", departmentDao.getDepartment(user.getDepartmentId()));
		engine.put("formDataDepartment", departmentDao.getDepartment(formData.getDepartmentId()));

		// execute scripts
		List<Script> scripts = formTemplateDao.get(formData.getFormTemplateId()).getScriptsByEvent(event);
		for (Script script : scripts) {
			if (script.isRowScript()) {
				executeRowScript(engine, script, formData, logger);
			} else {
				executeFormScript(engine, script, logger);
			}
		}
	}

	/**
	 * Выполняет скрипт для всей формы в целом.
	 *
	 * @param engine задает среду выполнения скрипта
	 * @param script скрипт формы
	 * @param logger логгер
	 */
	private void executeFormScript(ScriptEngine engine, Script script, Logger logger) {
		ScriptMessageDecorator d = new ScriptMessageDecorator();
		logger.setMessageDecorator(d);

		executeScript(engine, script, logger, d);

		logger.setMessageDecorator(null);
	}

	/**
	 * Выполняет скрипт для каждой строки формы.
	 *
	 * @param engine   задает среду выполнения скрипта
	 * @param script   скрипт строки
	 * @param formData данные формы
	 * @param logger   логгер
	 */
	private void executeRowScript(ScriptEngine engine, Script script, FormData formData, Logger logger) {
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

			decorator.setRowIndex(rowIndex);
			engine.put("row", row);
			engine.put("rowIndex", rowIndex);
			engine.put("rowAlias", row.getAlias());

			error = !executeScript(engine, script, logger, decorator);

			Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			bindings.remove("row");
			bindings.remove("rowIndex");
			bindings.remove("rowAlias");
		}

		logger.setMessageDecorator(null);
	}

	/**
	 * Выполняет любой скрипт, ловит ошибки, записывает их в логгер.
	 *
	 * @param engine    задает средуу выполнения скрипта
	 * @param script    скрипт
	 * @param logger    логгер
	 * @param decorator декоратор для ошибок
	 */
	private boolean executeScript(ScriptEngine engine, Script script, Logger logger, ScriptMessageDecorator decorator) {
		try {
			boolean executeCondition = true;

			// Condition
			if (script.getCondition() != null) {
				decorator.setScriptName(script.getName() + " - Условие исполнения");
				executeCondition = (Boolean) engine.eval(script.getCondition());
			}

			// If condition is absent or is true then execute script
			if (executeCondition) {
				decorator.setScriptName(script.getName());
				engine.eval(script.getBody());
			}

			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	/**
	 * Создает и возвращает среду выполнения для скрипта
	 *
	 * @return engine
	 */
	private ScriptEngine getScriptEngine() {
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("groovy");

		// Predefined imports
		CompilerConfiguration config = new CompilerConfiguration();
		ImportCustomizer ic = new ImportCustomizer();
		ic.addStarImports(PREDEFINED_IMPORTS);
		config.addCompilationCustomizers(ic);

		GroovyScriptEngineImpl groovyScriptEngine = (GroovyScriptEngineImpl) engine;
		GroovyClassLoader classLoader = groovyScriptEngine.getClassLoader();
		classLoader = new GroovyClassLoader(classLoader, config, false);
		groovyScriptEngine.setClassLoader(classLoader);

		Bindings b = engine.createBindings();
		b.putAll(scriptExposedBeans);
		engine.put("formDataDao", formDataDao);
		engine.setBindings(b, ScriptContext.ENGINE_SCOPE);
		return engine;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		scriptExposedBeans = new ConcurrentHashMap<String, Object>();
		scriptExposedBeans.putAll(context.getBeansOfType(ScriptExposed.class));
	}
}
