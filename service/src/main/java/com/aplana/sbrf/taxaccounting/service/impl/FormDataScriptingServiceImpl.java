package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.log.impl.ScriptMessageDecorator;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormDataScriptingService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import javax.script.Bindings;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Реализация сервиса для выполнения скриптов над формой.
 */
@Service
public class FormDataScriptingServiceImpl extends TAAbstractScriptingServiceImpl implements ApplicationContextAware, FormDataScriptingService {

    private static final String DUPLICATING_ARGUMENTS_ERROR = "The key \"%s\" already exists in map. Can't override of them.";

    @Autowired
    private FormTemplateDao formTemplateDao;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private Properties manifestProperties;

    @Override
    public void executeScript(TAUserInfo userInfo, FormData formData,
                              FormDataEvent event, Logger logger,
                              Map<String, Object> additionalParameters) {

        // Если скрипт отсутствует, то ничего не делаем
        String script = formTemplateDao.getFormTemplateScript(formData.getFormTemplateId());
        if (script == null || script.trim().isEmpty()) {
            return;
        }

        // Биндим параметры для выполнения скрипта
        Bindings b = scriptEngine.createBindings();

        Map<String, ?> scriptComponents =  getScriptExposedBeans(formData.getFormType().getTaxType(), event);
        for (Object component : scriptComponents.values()) {
            ScriptComponentContextImpl scriptComponentContext = new ScriptComponentContextImpl();
            scriptComponentContext.setUserInfo(userInfo);
            if (component instanceof ScriptComponentContextHolder){
                ((ScriptComponentContextHolder)component).setScriptComponentContext(scriptComponentContext);
            }
            if (additionalParameters != null && additionalParameters.containsKey("ip")) {
                scriptComponentContext.setUserInfo(userInfo);
            }
        }
        b.putAll(scriptComponents);
        b.put("formDataEvent", event);
        b.put("logger", logger);
        b.put("formData", formData);
        if (manifestProperties != null) {
            b.put("applicationVersion", manifestProperties.getProperty("Implementation-Version"));
            b.put("applicationRevision", manifestProperties.getProperty("X-Git"));
        }
        if (userInfo != null && userInfo.getUser() != null) {
            b.put("user", userInfo.getUser());
            b.put("userDepartment", departmentService.getDepartment(userInfo.getUser().getDepartmentId()));
        }
        b.put("formDataDepartment", departmentService.getDepartment(formData.getDepartmentId()));
        if (additionalParameters != null) {
            for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
                if (b.containsKey(entry.getKey()))
                    throw new IllegalArgumentException(String.format(DUPLICATING_ARGUMENTS_ERROR, entry.getKey()));
                b.put(entry.getKey(), entry.getValue());
            }
        }


        ScriptMessageDecorator d = new ScriptMessageDecorator(event.getTitle());
        logger.setMessageDecorator(d);

        executeScript(b, script, logger);

        logger.setMessageDecorator(null);

    }

    /**
     * Возвращает спринг-бины доcтупные для использования в скрипте.
     *
     * @param taxType тип налога
     * @param event событие
     */
    private Map<String, ?> getScriptExposedBeans(TaxType taxType, FormDataEvent event){
        Map<String, Object> beans = new HashMap<String, Object>();

        for(Map.Entry<String, ?> entry:applicationContext.getBeansWithAnnotation(ScriptExposed.class).entrySet()){
            Object bean = entry.getValue();
            ScriptExposed scriptExposed = AnnotationUtils.findAnnotation(bean.getClass(), ScriptExposed.class);
            boolean flag = true;

            if(scriptExposed.taxTypes().length>0){
                boolean has = false;

                for(TaxType tt: scriptExposed.taxTypes()){
                    if(taxType == tt){
                        has = true;
                    }
                }

                if(!has){
                    flag = false;
                }
            }

            if(scriptExposed.formDataEvents().length>0){
                boolean has = false;

                for(FormDataEvent ev:scriptExposed.formDataEvents()){
                    if(ev == event){
                        has = true;
                    }
                }

                if(!has){
                    flag = false;
                }
            }

            if(flag){
                beans.put(entry.getKey(), bean);
            }
        }

        return beans;
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
