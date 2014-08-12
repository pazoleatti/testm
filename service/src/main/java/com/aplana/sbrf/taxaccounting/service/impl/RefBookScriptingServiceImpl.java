package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

/**
 * Сервис, реализующий выполение скриптов справочников
 *
 * @author Dmitriy Levykin
 */
@Service
@Transactional
public class RefBookScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements RefBookScriptingService {

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

    @Override
    public void executeScript(TAUserInfo userInfo, long refBookId, FormDataEvent event, Logger logger, Map<String, Object> additionalParameters) {
        RefBook refBook = refBookFactory.get(refBookId);

        if (refBook == null || refBook.getScriptId() == null) {
            return;
        }

        BlobData bd = blobDataService.get(refBook.getScriptId());

        if (bd == null) {
            return;
        }

        StringWriter writer = new StringWriter();

        try {
            IOUtils.copy(bd.getInputStream(), writer, "UTF-8");
        } catch (IOException e) {
            logger.error(e);
            return;
        }
        String script = writer.toString();

        if (script == null || script.trim().isEmpty() || refBookFactory == null) {
            return;
        }

        // Локальный логгер для импорта конкретного справочника
        Logger scriptLogger = new Logger();

        // Биндиг параметров для выполнения скрипта
        Bindings bindings = scriptEngine.createBindings();
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
        bindings.put("refBookFactory", refBookFactory);

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

        // Откат при возникновении фатальных ошибок в скрипте
        if (scriptLogger.containsLevel(LogLevel.ERROR)) {
            String firstError = null;
            for (LogEntry logEntry : logger.getEntries()) {
                if (logEntry.getLevel() == LogLevel.ERROR) {
                    firstError = logEntry.getMessage();
                    break;
                }
            }
            throw new ServiceLoggerException("Произошли ошибки в скрипте справочника «"
                    + refBookDao.get(refBookId).getName() + "». " + firstError, logEntryService.save(logger.getEntries()));
        }
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
    public void saveScript(Long refBookId, String script) {

        RefBook refBook = refBookFactory.get(refBookId);
        if (!script.isEmpty() && script.trim().length() > 0) {

            InputStream inputStream;

            try {
                inputStream = IOUtils.toInputStream(script, "UTF-8");
            } catch (IOException e) {
                throw new ServiceException(ERROR_MSG, e);
            }

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
