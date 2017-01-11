package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
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
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Сервис, реализующий выполение скриптов справочников
 *
 * @author Dmitriy Levykin
 */
@Service
@Transactional
public class RefBookScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements RefBookScriptingService {

    private static final Log LOG = LogFactory.getLog(RefBookScriptingServiceImpl.class);
    private static final String DUPLICATING_ARGUMENTS_ERROR = "The key \"%s\" already exists in map. Can't override of them.";
    public static final String ERROR_MSG = "Ошибка при записи данных";

    @Autowired
    private BlobDataService blobDataService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private TemplateChangesService templateChangesService;
    @Autowired
    @Qualifier("versionInfoProperties")
    private Properties versionInfoProperties;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private AuditService auditService;

    @Override
    public boolean executeScript(TAUserInfo userInfo, long refBookId, FormDataEvent event, Logger logger, Map<String, Object> additionalParameters) {
        RefBook refBook = refBookFactory.get(refBookId);
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
        if (!canExecuteScript(script, event)) {
            return false;
        }

        boolean result = executeScript(userInfo, refBook, script, event, logger, additionalParameters);
        // Откат при возникновении фатальных ошибок в скрипте
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Проверка не пройдена (присутствуют фатальные ошибки)", logEntryService.save(logger.getEntries()));
        }
        return result;

    }

    @Override
    public boolean executeScriptInNewReadOnlyTransaction(final TAUserInfo userInfo, final RefBook refBook, final String script, final FormDataEvent event, final Logger logger, final Map<String, Object> additionalParameters) {
        boolean result = tx.executeInNewReadOnlyTransaction(new TransactionLogic<Boolean>() {
            @Override
            public Boolean execute() {
                return executeScript(userInfo, refBook, script, event, logger, additionalParameters);
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

    private boolean executeScript(TAUserInfo userInfo, RefBook refBook, String script, FormDataEvent event, Logger logger, Map<String, Object> additionalParameters) {
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
        String applicationVersion = "ФП «НДФЛ, Фонды и Сборы»";
        if (versionInfoProperties != null) {
            applicationVersion += " " + versionInfoProperties.getProperty("version");
        }
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
        scriptLogger.setMessageDecorator(new ScriptMessageDecorator("Событие «" + event.getTitle()
                + "» для справочника «" + refBook.getName() + "»"));
        executeScript(bindings, script, scriptLogger);
        scriptLogger.setMessageDecorator(null);

        // Перенос записей из локального лога в глобальный
        logger.getEntries().addAll(scriptLogger.getEntries());

        return true;
    }

    @Override
    public String getScript(Long refBookId) {
        StringWriter writer = new StringWriter();
        RefBook refBook = refBookFactory.get(refBookId);
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
    public void saveScript(long refBookId, String script, Logger log, TAUserInfo userInfo) {
        saveScript(refBookId, script, FormDataEvent.TEMPLATE_MODIFIED, log, userInfo);
        auditService.add(FormDataEvent.TEMPLATE_MODIFIED, userInfo, null, null,
                null, null, null, "Обнорвлен скрипт справочника \""+refBookFactory.get(refBookId).getName()+"\"", null);
    }

    @Override
    public void importScript(long refBookId, String script, Logger log, TAUserInfo userInfo) {
        try {
            saveScript(refBookId, script, FormDataEvent.SCRIPTS_IMPORT, log, userInfo);
        } catch (ServiceLoggerException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return;
        }
        auditService.add(FormDataEvent.SCRIPTS_IMPORT, userInfo, null, null,
                null, null, null, "Обнорвлен скрипт справочника \""+refBookFactory.get(refBookId).getName()+"\"", null);
    }

    private void saveScript(long refBookId, String script, FormDataEvent formDataEvent, Logger log, TAUserInfo userInfo) {
        RefBook refBook = refBookFactory.get(refBookId);
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
                refBookDao.setScriptId(refBookId, uuid);
            } else {
                blobDataService.save(refBook.getScriptId(), inputStream);
            }
        } else {
            if (refBook.getScriptId() != null) {
                refBookDao.setScriptId(refBookId, null);
                blobDataService.delete(refBook.getScriptId());
            }
        }
        TemplateChanges templateChanges = new TemplateChanges();
        templateChanges.setRefBookId(Long.valueOf(refBookId).intValue());
        templateChanges.setEvent(formDataEvent);
        templateChanges.setAuthor(userInfo.getUser());
        templateChanges.setEventDate(new Date());
        templateChangesService.save(templateChanges);
    }

    /**
     * Перехват ошибок и исключений
     *
     * @param bindings
     * @param script
     * @param logger
     * @return
     */
    private boolean executeScript(Bindings bindings, String script, Logger logger) {
        try {
            getScriptEngine().eval(script, bindings);
            return true;
        } catch (ScriptException e) {
            int i = ExceptionUtils.indexOfThrowable(e, ScriptServiceException.class);
            if (i != -1) {
                throw (ScriptServiceException)ExceptionUtils.getThrowableList(e).get(i);
            }
            logScriptException(e, logger);
            return false;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
}