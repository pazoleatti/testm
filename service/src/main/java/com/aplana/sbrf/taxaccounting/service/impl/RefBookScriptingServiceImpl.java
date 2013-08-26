package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.RefBookScriptingService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.Bindings;
import javax.script.ScriptException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
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

    @Autowired
    private BlobDataDao blobDao;

    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public void executeScript(TAUserInfo userInfo, long refBookId, FormDataEvent event, Logger logger, Map<String, Object> additionalParameters) {
        RefBook refBook = refBookFactory.get(refBookId);

        if (refBook == null || refBook.getScriptId() == null) {
            return;
        }

        BlobData bd = blobDao.get(refBook.getScriptId());

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

        // Биндиг параметров для выполнения скрипта
        Bindings bindings = scriptEngine.createBindings();
        // ScriptExposed
        Map<String, ?> scriptComponents = applicationContext.getBeansWithAnnotation(ScriptExposed.class);
        for (Object component : scriptComponents.values()) {
            if (component instanceof ScriptComponentContextHolder) {
                ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
                scriptComponentContext.setUserInfo(userInfo);
                scriptComponentContext.setLogger(logger);
                ((ScriptComponentContextHolder) component).setScriptComponentContext(scriptComponentContext);
            }
        }
        bindings.putAll(scriptComponents);

        bindings.put("formDataEvent", event);
        bindings.put("logger", logger);
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

        logger.setMessageDecorator(new ScriptMessageDecorator(event.getTitle()));
        executeScript(bindings, script, logger);
        logger.setMessageDecorator(null);
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
