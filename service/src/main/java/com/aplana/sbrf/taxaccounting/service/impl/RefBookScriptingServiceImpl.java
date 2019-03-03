package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TemplateChanges;
import com.aplana.sbrf.taxaccounting.model.exception.ScriptServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;
import com.aplana.sbrf.taxaccounting.service.TemplateChangesService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;

/**
 * Сервис, реализующий выполение скриптов справочников
 */
@Service
@Transactional
public class RefBookScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements RefBookScriptingService {

    private static final Log LOG = LogFactory.getLog(RefBookScriptingServiceImpl.class);
    private static final String DUPLICATING_ARGUMENTS_ERROR = "The key \"%s\" already exists in map. Can't override of them.";
    private static final String ERROR_MSG = "Ошибка при записи данных";

    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private TemplateChangesService templateChangesService;
    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private AuditService auditService;

    private final static String SCRIPT_PATH_PREFIX = "../src/main/resources/refbook";

    @Override
    public boolean executeScript(TAUserInfo userInfo, long refBookId, FormDataEvent event, Logger logger, Map<String, Object> additionalParameters) {
        RefBook refBook = commonRefBookService.get(refBookId);
        if (refBook == null || refBook.getScriptId() == null) {
            return false;
        }
        BlobData bd = blobDataService.get(refBook.getScriptId());
        if (bd == null) {
            return false;
        }
        // извлекаем скрипт
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(bd.getInputStream(), writer, "UTF-8");
        } catch (IOException e) {
            logger.error(e);
            return false;
        }
        String script = writer.toString();
        String scriptFilePath = null;
        if (!applicationInfo.isProductionMode()) {
            scriptFilePath = getScriptFilePath(getPackageName(script), SCRIPT_PATH_PREFIX, logger, event);
            if (scriptFilePath != null) {
                script = getScript(scriptFilePath);
            }
        }
        if (!canExecuteScript(script, event)) {
            return false;
        }

        boolean result = executeScript(userInfo, script, scriptFilePath, event, logger, additionalParameters);
        // Откат при возникновении фатальных ошибок в скрипте
        if (logger.containsLevel(LogLevel.ERROR)) {
            if (event.equals(FormDataEvent.CREATE_APPLICATION_2)) {
                throw new ServiceException();
            } else {
                throw new ServiceLoggerException("Проверка не пройдена (присутствуют фатальные ошибки)", logEntryService.save(logger.getEntries()));
            }
        }
        return result;

    }

    @Override
    protected String getPackageName(String script) {
        try {
            String packageWord = "package";
            String[] scriptLines = script.split("\\r\\n|\\n|\\r");
            String packageName = scriptLines[0].substring(script.indexOf(packageWord) + packageWord.length() + 1, scriptLines[0].indexOf("//")).trim();
            String searchComment = scriptLines[0].substring(scriptLines[0].indexOf("//") + 3).trim();
            String fileName = searchComment.substring(0, searchComment.indexOf(" ")).trim();
            return packageName + "." + fileName;
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean executeScriptInNewReadOnlyTransaction(final TAUserInfo userInfo, final RefBook refBook, final String script, final FormDataEvent event, final Logger logger, final Map<String, Object> additionalParameters) {
        boolean result = tx.executeInNewReadOnlyTransaction(new TransactionLogic<Boolean>() {
            @Override
            public Boolean execute() {
                return executeScript(userInfo, script, null, event, logger, additionalParameters);
            }
        });
        return result;
    }

    private void checkScript(RefBook refBook, String script, final Logger logger) {
        Logger tempLogger = new Logger();
        try {
            executeScriptInNewReadOnlyTransaction(null, refBook, script, FormDataEvent.CHECK_SCRIPT, tempLogger, null);
            if (tempLogger.containsLevel(LogLevel.ERROR)) {
                logger.getEntries().addAll(tempLogger.getEntries());
                throw new ServiceException("Обнаружены ошибки в скрипте!");
            }
        } catch (Exception ex) {
            tempLogger.error(ex);
            logger.getEntries().addAll(tempLogger.getEntries());
            throw new ServiceLoggerException("Обнаружены ошибки в скрипте!", logEntryService.save(logger.getEntries()));
        }
        logger.getEntries().addAll(tempLogger.getEntries());
    }

    private boolean executeScript(TAUserInfo userInfo, String script, String scriptFilePath, FormDataEvent event, Logger logger, Map<String, Object> additionalParameters) {
        // Локальный логгер для импорта конкретного справочника
        Logger scriptLogger = new Logger();
        scriptLogger.setTaUserInfo(userInfo);

        // Биндиг параметров для выполнения скрипта
        Bindings bindings = getScriptEngine().createBindings();
        bindings.put("dataSource", applicationContext.getBean("dataSource"));

        // ScriptExposed
        Map<String, ?> scriptComponents = applicationContext.getBeansWithAnnotation(ScriptExposed.class);
        for (Object component : scriptComponents.values()) {
            if (component instanceof ScriptComponentContextHolder) {
                ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
                scriptComponentContext.setUserInfo(userInfo);
                scriptComponentContext.setLogger(scriptLogger);
                ((ScriptComponentContextHolder) component).setScriptComponentContext(scriptComponentContext);
            }
        }
        bindings.putAll(scriptComponents);

        bindings.put("formDataEvent", event);
        bindings.put("logger", scriptLogger);
        bindings.put("userInfo", userInfo);
        bindings.put("refBookFactory", refBookFactory);
        bindings.put("commonRefBookService", commonRefBookService);

        String applicationVersion = "ФП «НДФЛ»";
        applicationVersion += " " + applicationInfo.getVersion();
        bindings.put("applicationVersion", applicationVersion);

        if (userInfo != null && userInfo.getUser() != null) {
            bindings.put("user", userInfo.getUser());
        }

        // Перенос доп. параметров
        if (additionalParameters != null) {
            for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
                if (bindings.containsKey(entry.getKey()))
                    throw new IllegalArgumentException(String.format(DUPLICATING_ARGUMENTS_ERROR, entry.getKey()));
                bindings.put(entry.getKey(), entry.getValue());
            }
        }

        // Выполнение импорта скрипта справочника
        if (scriptFilePath == null || applicationInfo.isProductionMode()) {
            executeScript(bindings, script, scriptLogger);
        } else {
            executeLocalScript(toBinding(bindings), scriptFilePath, logger);
        }

        // Перенос записей из локального лога в глобальный
        logger.getEntries().addAll(scriptLogger.getEntries());

        return true;
    }

    @Override
    public String getScript(Long refBookId) {
        StringWriter writer = new StringWriter();
        RefBook refBook = commonRefBookService.get(refBookId);
        if (refBook.getScriptId() != null) {
            BlobData blobData = blobDataService.get(refBook.getScriptId());
            try {
                IOUtils.copy(blobData.getInputStream(), writer, "UTF-8");
            } catch (IOException e) {
                throw new ServiceException(ERROR_MSG, e);
            }
        }
        return writer.toString();
    }

    @Override
    public void importScript(long refBookId, String script, Logger log, TAUserInfo userInfo) {
        try {
            saveScript(refBookId, script, log, userInfo);
        } catch (ServiceLoggerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return;
        }
        auditService.add(FormDataEvent.SCRIPTS_IMPORT, userInfo, null, null,
                null, null, null, null, "Обнорвлен скрипт справочника \"" + commonRefBookService.get(refBookId).getName() + "\"", null);
    }

    private void saveScript(long refBookId, String script, Logger log, TAUserInfo userInfo) {
        RefBook refBook = commonRefBookService.get(refBookId);
        if (script != null && !script.trim().isEmpty()) {
            InputStream inputStream;
            try {
                inputStream = IOUtils.toInputStream(script, "UTF-8");
            } catch (IOException e) {
                throw new ServiceException(ERROR_MSG, e);
            }

            checkScript(refBook, script, log);
            if (refBook.getScriptId() == null) {
                String uuid = blobDataService.create(inputStream, refBook.getName());
                refBookDao.updateScriptId(refBookId, uuid);
            } else {
                blobDataService.save(refBook.getScriptId(), inputStream);
            }
        } else {
            if (refBook.getScriptId() != null) {
                refBookDao.updateScriptId(refBookId, null);
                blobDataService.delete(refBook.getScriptId());
            }
        }
        TemplateChanges templateChanges = new TemplateChanges();
        templateChanges.setRefBookId(Long.valueOf(refBookId).intValue());
        templateChanges.setEvent(FormDataEvent.SCRIPTS_IMPORT);
        templateChanges.setAuthor(userInfo.getUser());
        templateChanges.setEventDate(new Date());
        templateChangesService.save(templateChanges);
    }

    /**
     * Перехват ошибок и исключений
     */
    private boolean executeScript(Bindings bindings, String script, Logger logger) {
        try {
            getScriptEngine().eval(script, bindings);
            return true;
        } catch (ScriptException e) {
            int i = ExceptionUtils.indexOfThrowable(e, ScriptServiceException.class);
            if (i != -1) {
                throw (ScriptServiceException) ExceptionUtils.getThrowableList(e).get(i);
            }
            logScriptException(e, logger);
            return false;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
}