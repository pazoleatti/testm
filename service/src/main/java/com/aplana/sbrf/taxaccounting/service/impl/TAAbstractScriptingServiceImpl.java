package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.groovy.jsr223.GroovyScriptEngine;
import com.aplana.sbrf.taxaccounting.groovy.jsr223.GroovyScriptEngineFactory;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Базовый класс для сервисов, работающих с groovy-скриптами
 *
 * @author dsultanbekov
 */
public abstract class TAAbstractScriptingServiceImpl implements ApplicationContextAware {

    private static final Log LOG = LogFactory.getLog(TAAbstractScriptingServiceImpl.class);
    /**
     * Регулярка для поиска номера строки ошибки в теле скрипта
     */
    private static final Pattern REGEXP = Pattern.compile("^.*Script[0-9]+$");
    /**
     * Регулярка для поиска комментариев в коде
     */
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

    private GroovyScriptEngine groovyScriptEngine;
    protected CompilerConfiguration config;

    public TAAbstractScriptingServiceImpl() {
        ScriptEngineManager factory = new ScriptEngineManager();
        factory.registerEngineName("groovy", new GroovyScriptEngineFactory());
        groovyScriptEngine = (GroovyScriptEngine) factory.getEngineByName("groovy");

        // Predefined imports
        config = new CompilerConfiguration();
        ImportCustomizer ic = new ImportCustomizer();
        ic.addStarImports(PREDEFINED_IMPORTS);
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

    protected void logScriptException(Exception e, Logger logger) {
        String message = e.getMessage();
        Throwable t = e;
        Throwable rootCause = e;
        while (t != null) {
            message = t.getMessage();
            rootCause = t;
            t = t.getCause();
        }
        // TODO: поискать более красивый способ для определения номера строки, в которой произошла ошибка
        // К сожалению, использовать информацию из ScriptException.getLineNumber() не получается - там всегда -1
        int line = -1;
        for (StackTraceElement stackElement : rootCause.getStackTrace()) {
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
     *
     * @param script проверяемый скрипт. Может быть null.
     * @param event  тип события. Может быть null - тогда не ведется поиск обработчика внутри скрипта.
     * @return true - запускать скрипт можно; false - не стоит
     */
    public static boolean canExecuteScript(String script, FormDataEvent event) {
        if (StringUtils.isBlank(script)) {
            return false;
        }
        if (event == null) {
            return true;
        }
        if (!script.contains("FormDataEvent." + event.name())) {
            return false;
        }
        // убираю комментарии из скрипта
        Matcher matcher = COMMENT_REGEXP.matcher(script);
        String noCommentString = matcher.replaceAll("");
        Pattern pattern = Pattern.compile("\\s*case\\s+FormDataEvent." + event.name() + "\\s*:");
        matcher = pattern.matcher(noCommentString);
        return matcher.find();
    }

    protected String getPackageName(String script) {
        try {
            String packageWord = "package";
            String scriptLines[] = script.split("\\r\\n|\\n|\\r");
            return scriptLines[0].substring(script.indexOf(packageWord) + packageWord.length() + 1).trim();
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }

    }

    public static String getScriptFilePath(String packageName, String scriptPathPrefix, Logger logger, FormDataEvent event) {
        String scriptFilePath = null;
        try {
            File folder = new File(scriptPathPrefix);

            String eventScript = findEventScript(folder, packageName, event);
            if (eventScript == null) {
                return findLocalScriptPath(folder, packageName);
            } else {
                return eventScript;
            }
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            logger.error("Не удалось получить локальный скрипт", e);
        }
        return scriptFilePath;
    }

    public static String findLocalScriptPath(File folder, String packageName) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                //Если папка - достаем из нее файлы groovy
                String script = findLocalScriptPath(file, packageName);
                if (script != null) {
                    return script;
                }
            } else {
                if (file.getName().endsWith(".groovy")) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    try {
                        String line = reader.readLine();
                        if (line != null && line.equals("package " + packageName) && line.contains(file.getName().substring(0, file.getName().indexOf(".groovy")))
                                || packageName.equals("refbook." + file.getName().substring(0, file.getName().indexOf(".groovy")) + "_ref")) {
                            return file.getAbsolutePath();
                        }
                    } finally {
                        reader.close();
                    }
                }
            }
        }
        return null;
    }

    private static String findEventScript(File folder, String packageName, FormDataEvent event) throws IOException {

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                String script = findEventScript(file, packageName, event);
                if (script != null) {
                    return script;
                }
            } else if (file.getName().contains("_" + event.name().toLowerCase() + ".")) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                try {
                    String line = reader.readLine();
                    if (line.equals("package " + packageName)) {
                        return file.getAbsolutePath();
                    }
                } finally {
                    reader.close();
                }
            }
        }
        return null;
    }

    /**
     * Получить текст скрипта
     *
     * @param filePath
     * @return
     */
    public static String getScript(String filePath) {
        FileReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            reader = new FileReader(filePath);
            // читаем посимвольно
            int c;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return stringBuilder.toString();
    }

    protected static Binding toBinding(Bindings bindings) {
        Binding binding = new Binding();
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            binding.setVariable(entry.getKey(), entry.getValue());
        }
        return binding;
    }


    protected boolean executeLocalScript(Binding binding, String scriptFilePath, Logger logger) {
        try {
            File scriptFile = new File(scriptFilePath);
            config.setSourceEncoding("UTF-8");
            GroovyShell groovyShell = new GroovyShell(binding, config);
            groovyShell.evaluate(scriptFile);
            return true;
        } catch (Exception e) {
            logScriptException(e, logger);
            throw new ServiceException("%s", e.toString());
        }
    }
}
