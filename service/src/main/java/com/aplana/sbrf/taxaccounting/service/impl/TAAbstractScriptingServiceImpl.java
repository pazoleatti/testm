package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.groovy.jsr223.GroovyScriptEngine;
import com.aplana.sbrf.taxaccounting.groovy.jsr223.GroovyScriptEngineFactory;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Базовый класс для сервисов, работающих с groovy-скриптами
 * @author dsultanbekov
 */
public abstract class TAAbstractScriptingServiceImpl implements ApplicationContextAware {

	private static final Log LOG = LogFactory.getLog(TAAbstractScriptingServiceImpl.class);
	/** Регулярка для поиска номера строки ошибки в теле скрипта */
	private static final Pattern REGEXP = Pattern.compile("^.*Script[0-9]+$");
	/** Регулярка для поиска комментариев в коде */
	private static final Pattern COMMENT_REGEXP = Pattern.compile("(//.*)|((?s)/\\*.*?\\*/)");

	protected ApplicationContext applicationContext;
	
	/**
	 * Предопределенные пакеты для импорта в скрипты. Здесь только пакеты.
	 */
	private static final String[] PREDEFINED_IMPORTS = new String[]{
			"com.aplana.sbrf.taxaccounting.model",
			"com.aplana.sbrf.taxaccounting.model.dictionary",
			"com.aplana.sbrf.taxaccounting.model.LOG",
			"com.aplana.sbrf.taxaccounting.model.script.range",
			"com.aplana.sbrf.taxaccounting.model.refbook",
			"com.aplana.sbrf.taxaccounting.model.util",
			"com.aplana.sbrf.taxaccounting.model.datarow",
			"com.aplana.sbrf.taxaccounting.dao.exсeption"
	};

	private static final String[] PREDEFINED_STATIC_IMPORTS = new String[]{
			"com.aplana.sbrf.taxaccounting.service.script.util.ScriptUtils"
	};

	private GroovyScriptEngine groovyScriptEngine;

	public TAAbstractScriptingServiceImpl() {
		ScriptEngineManager factory = new ScriptEngineManager();
        factory.registerEngineName("groovy", new GroovyScriptEngineFactory());
		groovyScriptEngine = (GroovyScriptEngine)factory.getEngineByName("groovy");

		// Predefined imports
        CompilerConfiguration config = new CompilerConfiguration();
		ImportCustomizer ic = new ImportCustomizer();
		ic.addStarImports(PREDEFINED_IMPORTS);
		ic.addStaticStars(PREDEFINED_STATIC_IMPORTS);
		config.addCompilationCustomizers(ic);

        GroovyClassLoader classLoader = groovyScriptEngine.getClassLoader();
		classLoader = new GroovyClassLoader(classLoader, config, false);
		groovyScriptEngine.setClassLoader(classLoader);
	}

    protected ScriptEngine getScriptEngine() {
        return groovyScriptEngine;
    }

	@Override
	public void setApplicationContext(ApplicationContext context) {
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
			Matcher matcher = REGEXP.matcher(stackElement.getClassName());
            if (matcher.matches() && stackElement.getFileName().endsWith(".groovy")) {
				line = stackElement.getLineNumber();
				break;
			}
		}
		logger.error("Ошибка исполнения [%d]: %s", line, message);
		this.LOG.error("An error occured during script execution", e);
	}

	/**
	 * Проверяет целесообразность запуска скрипта для указанного события. Если скрипт пустой или не содержит
	 * обработчика указанного события, то он выполняться не будет.
	 * @param script проверяемый скрипт. Может быть null.
	 * @param event тип события. Может быть null - тогда не ведется поиск обработчика внутри скрипта.
	 * @return true - запускать скрипт можно; false - не стоит
	 */
	public static boolean canExecuteScript(String script, FormDataEvent event) {
		if (StringUtils.isBlank(script)) {
			return false;
		}
		if (event == null) {
			return true;
		}
		// убираю комментарии из скрипта
		Matcher matcher = COMMENT_REGEXP.matcher(script);
		String noCommentString = matcher.replaceAll("");
		Pattern pattern = Pattern.compile("\\s*case\\s+FormDataEvent." + event.name() + "\\s*:");
		matcher = pattern.matcher(noCommentString);
		return matcher.find();
	}
}
