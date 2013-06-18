package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptException;

import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.service.script.ScriptComponentContextImpl;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Реализация сервиса для запуска скриптов по декларациями
 * @author dsultanbekov
 * @author mfayzullin
 */
@Component
public class DeclarationDataScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements DeclarationDataScriptingService {
	
	private static final String DUPLICATING_ARGUMENTS_ERROR = "The key \"%s\" already exists in map. Can't override of them.";

	@Autowired
	private DeclarationTemplateDao declarationTemplateDao;
	
	/**
	 * Возвращает спринг-бины доcтупные для использования в скрипте создания декларации.
	 * @param taxType вид налога
	 */
	private Map<String, ?> getScriptExposedBeans(TaxType taxType, FormDataEvent event){
		Map<String, Object> beans = new HashMap<String, Object>();

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
	public void executeScript(TAUserInfo userInfo, DeclarationData declarationData, FormDataEvent event, Logger logger,
			Map<String, Object> exchangeParams) {
		this.logger.debug("Starting processing request to run create script");
		
		DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());
		
		// Биндим параметры для выполнения скрипта
		Bindings b = scriptEngine.createBindings();
		
		Map<String, ?> scriptComponents =  getScriptExposedBeans(declarationTemplate.getDeclarationType().getTaxType(), event);
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
		b.put("declarationData", declarationData);
		if (exchangeParams != null) {
			for (Map.Entry<String, Object> entry : exchangeParams.entrySet()) {
				if (b.containsKey(entry.getKey()))
					throw new IllegalArgumentException(String.format(DUPLICATING_ARGUMENTS_ERROR, entry.getKey()));
				b.put(entry.getKey(), entry.getValue());
			}
		}

		ScriptMessageDecorator d = new ScriptMessageDecorator(event.getTitle());
		logger.setMessageDecorator(d);
			
		executeScript(b, declarationTemplate.getCreateScript(), logger, d);
			
		logger.setMessageDecorator(null);

		
		if (logger.containsLevel(LogLevel.ERROR)) {
			throw new ServiceLoggerException(
					"Есть критические ошибки при выполнения скрипта",
					logger.getEntries());
		}
	}

	private boolean executeScript(Bindings bindings, String script, Logger logger, ScriptMessageDecorator decorator) {
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
