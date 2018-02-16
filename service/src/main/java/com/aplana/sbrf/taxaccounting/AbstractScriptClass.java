package com.aplana.sbrf.taxaccounting;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;

public abstract class AbstractScriptClass {
    protected Logger logger;
    protected FormDataEvent formDataEvent;
    protected groovy.lang.Script scriptClass;
    protected DeclarationService declarationService;
    protected ConfigurationService configurationService;
    protected TAUserInfo userInfo;

    protected boolean showTiming;
    protected int similarityThreshold;

    private AbstractScriptClass() {
    }

    @SuppressWarnings("unchecked")
    public AbstractScriptClass(groovy.lang.Script scriptClass) {
        this.scriptClass = scriptClass;
        if (scriptClass.getBinding().hasVariable("logger")) {
            this.logger = (Logger) scriptClass.getProperty("logger");
        }
        if (scriptClass.getBinding().hasVariable("formDataEvent")) {
            this.formDataEvent = (FormDataEvent) scriptClass.getProperty("formDataEvent");
        }
        if (scriptClass.getBinding().hasVariable("declarationService")) {
            this.declarationService = (DeclarationService) scriptClass.getProperty("declarationService");
        }
        if (scriptClass.getBinding().hasVariable("configurationService")) {
            this.configurationService = (ConfigurationService) scriptClass.getProperty("configurationService");
        }
        if (scriptClass.getBinding().hasVariable("userInfo")) {
            this.userInfo = (TAUserInfo) scriptClass.getProperty("userInfo");
        }

    }

    public abstract void run();

    protected void initConfiguration(){
        final ConfigurationParamModel configurationParamModel = configurationService.getCommonConfigUnsafe();
        String showTiming = configurationParamModel.get(ConfigurationParam.SHOW_TIMING).get(0).get(0);
        String limitIdent = configurationParamModel.get(ConfigurationParam.LIMIT_IDENT).get(0).get(0);
        if (showTiming.equals("1")) {
            this.showTiming = true;
        }
        similarityThreshold = limitIdent != null ? (int) (Double.valueOf(limitIdent) * 1000) : 0;
    }

    protected void logForDebug(String message, Object... args) {
        if (showTiming) {
            logger.info(message, args);
        }
    }
}
