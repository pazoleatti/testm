package com.aplana.sbrf.taxaccounting.util;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.impl.TAAbstractScriptingServiceImpl;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Сервис-обертка для выполнения groovy-скриптов и обработки ошибок
 *
 * @author Levykin
 */
public class ScriptingService extends TAAbstractScriptingServiceImpl {
    public ScriptEngine getEngine() {
        return getScriptEngine();
    }

    public void logScriptException(ScriptException e, Logger logger) {
        super.logScriptException(e, logger);
    }
}
