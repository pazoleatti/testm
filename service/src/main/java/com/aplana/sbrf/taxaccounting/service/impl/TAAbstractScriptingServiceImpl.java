package com.aplana.sbrf.taxaccounting.service.impl;

import groovy.lang.GroovyClassLoader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.aplana.sbrf.taxaccounting.model.log.Logger;

/**
 * Базовый класс для сервисов, работающих с groovy-скриптами
 * @author dsultanbekov
 */
public abstract class TAAbstractScriptingServiceImpl implements ApplicationContextAware {

	protected Log logger = LogFactory.getLog(getClass());
	
	protected ApplicationContext applicationContext;
	
	/**
	 * Предопределенные пакеты для импорта в скрипты. Здесь только пакеты.
	 */
	private static final String[] PREDEFINED_IMPORTS = new String[]{
			"com.aplana.sbrf.taxaccounting.model",
			"com.aplana.sbrf.taxaccounting.model.dictionary",
			"com.aplana.sbrf.taxaccounting.model.log",
			"com.aplana.sbrf.taxaccounting.model.script.range",
			"com.aplana.sbrf.taxaccounting.dao.exсeption"
	};

	private static final String[] PREDEFINED_STATIC_IMPORTS = new String[]{
			"com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils"
	};
	
	protected ScriptEngine scriptEngine;	

	public TAAbstractScriptingServiceImpl() {
		ScriptEngineManager factory = new ScriptEngineManager();
		this.scriptEngine = factory.getEngineByName("groovy");

		// Predefined imports
		CompilerConfiguration config = new CompilerConfiguration();
		ImportCustomizer ic = new ImportCustomizer();
		ic.addStarImports(PREDEFINED_IMPORTS);
		ic.addStaticStars(PREDEFINED_STATIC_IMPORTS);
		config.addCompilationCustomizers(ic);

		GroovyScriptEngineImpl groovyScriptEngine = (GroovyScriptEngineImpl) this.scriptEngine;
		GroovyClassLoader classLoader = groovyScriptEngine.getClassLoader();
		classLoader = new GroovyClassLoader(classLoader, config, false);
		groovyScriptEngine.setClassLoader(classLoader);
	}
	

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.applicationContext = context;
	}
	
	protected void logScriptException(ScriptException e, Logger logger) {
		String message = e.getMessage();
		Throwable t = e;
		Throwable rootCause = e;
		while(t != null) {
			message = t.getMessage();
			rootCause = t;
			t = t.getCause();
		}
		// TODO: поискать более красивый способ для определения номера строки, в которой произошла ошибка
		// К сожалению, использовать информацию из ScriptException.getLineNumber() не получается - там всегда -1
		int line = -1;
		for (StackTraceElement stackElement: rootCause.getStackTrace()) {
			String className = stackElement.getClassName();
			if (className.matches("^Script[0-9]+$") && stackElement.getFileName().endsWith(".groovy")) {
				line = stackElement.getLineNumber();
				break;
			}
		}			
		logger.error("Ошибка исполнения [строка %d]: %s", line, message);
		this.logger.error("An error occured during script execution", e);
	}
	
}
