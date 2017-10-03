package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateEventScriptDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ScriptServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import groovy.lang.Binding;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

/**
 * Реализация сервиса для запуска скриптов по декларациями
 * @author dsultanbekov
 * @author mfayzullin
 */
@Component
public class DeclarationDataScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements DeclarationDataScriptingService {

	private static final Log LOG = LogFactory.getLog(DeclarationDataScriptingServiceImpl.class);
	private static final String DUPLICATING_ARGUMENTS_ERROR = "The key \"%s\" already exists in map. Can't override of them.";

	@Autowired
	private DeclarationTemplateDao declarationTemplateDao;
	@Autowired
	private DeclarationTemplateEventScriptDao declarationTemplateEventScriptDao;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private TransactionHelper tx;

	private final static String SCRIPT_PATH_PREFIX = "../src/main/resources/form_template";

	/**
	 * Возвращает спринг-бины доступные для использования в скрипте создания декларации.
	 * @param taxType вид налога
	 */
	private Map<String, ?> getScriptExposedBeans(TaxType taxType, FormDataEvent event){
		Map<String, Object> beans = new HashMap<String, Object>();

        beans.put("dataSource", applicationContext.getBean("dataSource"));

		for(Map.Entry<String, ?> entry:applicationContext.getBeansWithAnnotation(ScriptExposed.class).entrySet()){
			Object bean = entry.getValue();
			ScriptExposed scriptExposed = AnnotationUtils.findAnnotation(bean.getClass(), ScriptExposed.class);
			boolean flag = true;

			if(scriptExposed.taxTypes().length>0){
				flag = false;

				for(TaxType tt: scriptExposed.taxTypes()){
					if(taxType == tt){
						flag = true;
						break;
					}
				}
			}
			if(flag){
				beans.put(entry.getKey(), bean);
			}
		}
		return beans;
	}

	@Override
	public boolean executeScript(TAUserInfo userInfo, DeclarationData declarationData, FormDataEvent event, Logger logger,
								 Map<String, Object> exchangeParams) {
        LOG.debug("Starting processing request to run create script");
        String script = declarationTemplateDao.getDeclarationTemplateScript(declarationData.getDeclarationTemplateId());
		String scriptFilePath = null;
		if (!applicationInfo.isProductionMode()) {
			scriptFilePath = getScriptFilePath(getPackageName(script), SCRIPT_PATH_PREFIX, logger, event);
			if (scriptFilePath != null) {
				script = getScript(scriptFilePath);
			}
		} else {
			String eventScript = declarationTemplateEventScriptDao.findScript(declarationData.getDeclarationTemplateId(), event.getCode());
			if (eventScript != null) {
				script = eventScript;
			}
		}
		if (!canExecuteScript(script, event)) {
            return false;
        }
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());
		if (scriptFilePath == null) {
			declarationTemplate.setCreateScript(script);
		}
        return executeScript(userInfo, declarationTemplate, declarationData, scriptFilePath, event, logger, exchangeParams);
    }

    @Override
    public boolean executeScriptInNewReadOnlyTransaction(final TAUserInfo userInfo, final DeclarationTemplate declarationTemplate, final DeclarationData declarationData, final FormDataEvent event,
                                                         final Logger logger, final Map<String, Object> exchangeParams) {
        return tx.executeInNewReadOnlyTransaction(new TransactionLogic<Boolean>() {
            @Override
            public Boolean execute() {
                return executeScript(userInfo, declarationTemplate, declarationData, null, event, logger, exchangeParams);
            }
        });
    }

    private boolean executeScript(TAUserInfo userInfo, DeclarationTemplate declarationTemplate, DeclarationData declarationData, String scriptFilePath, FormDataEvent event, Logger logger,
                          Map<String, Object> exchangeParams) {
		// Биндим параметры для выполнения скрипта
		Bindings b = getScriptEngine().createBindings();
		Binding binding = new Binding();

		Map<String, ?> scriptComponents = getScriptExposedBeans(TaxType.NDFL, event);
		for (Object component : scriptComponents.values()) {
			ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
			scriptComponentContext.setUserInfo(userInfo);
			scriptComponentContext.setLogger(logger);
			if (component instanceof ScriptComponentContextHolder){
				((ScriptComponentContextHolder)component).setScriptComponentContext(scriptComponentContext);
			}
		}
		b.putAll(scriptComponents);

		b.put("formDataEvent", event);
		b.put("logger", logger);
        b.put("userInfo", userInfo);
		b.put("declarationData", declarationData);

		String applicationVersion = "ФП «НДФЛ, Фонды и Сборы»";
		applicationVersion += " " + applicationInfo.getVersion();
        b.put("applicationVersion", applicationVersion);

		if (exchangeParams != null) {
			for (Map.Entry<String, Object> entry : exchangeParams.entrySet()) {
				if (b.containsKey(entry.getKey()))
					throw new IllegalArgumentException(String.format(DUPLICATING_ARGUMENTS_ERROR, entry.getKey()));
				b.put(entry.getKey(), entry.getValue());
			}
		}

		if (scriptFilePath == null || applicationInfo.isProductionMode()) {
			executeScript(b, declarationTemplate.getCreateScript(), logger);
		} else {
			executeLocalScript(toBinding(b), scriptFilePath, logger);
		}

		return true;
	}

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
            //TODO: Добавить вывод номера строки в скрипте
			logger.error(new ServiceException("Обнаружены ошибки в скрипте!", e));
			return false;
		}
	}
}