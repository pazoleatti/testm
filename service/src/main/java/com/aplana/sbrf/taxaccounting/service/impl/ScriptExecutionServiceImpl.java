package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.ScriptExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.Bindings;
import javax.script.ScriptException;

/**
 * Реализация сервиса для выполнения скриптов над формой.
 *
 * @author Stanislav Yasinskiy
 */
@Service
@Transactional
public class ScriptExecutionServiceImpl extends TAAbstractScriptingServiceImpl implements ApplicationContextAware, ScriptExecutionService {

    @Autowired
    private LogEntryService logEntryService;

    @Override
    public void executeScript(TAUserInfo userInfo, String script, Logger logger) {

        // Биндим параметры для выполнения скрипта
        Bindings b = scriptEngine.createBindings();

        b.put("formDataEvent", FormDataEvent.TEST);
        b.put("logger", logger);
        b.put("user", userInfo.getUser());

        ScriptMessageDecorator d = new ScriptMessageDecorator(FormDataEvent.TEST.getTitle());
        logger.setMessageDecorator(d);

        executeScript(b, script, logger);

        logger.setMessageDecorator(null);

        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException("Найдены ошибки при выполнении расчета формы", logEntryService.save(logger.getEntries()));
        } else {
            logger.info("Скрипт успешно выполнен");
        }
    }

    private void executeScript(Bindings bindings, String script, Logger logger) {
        try {
            scriptEngine.eval(script, bindings);
        } catch (ScriptException e) {
            logScriptException(e, logger);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
