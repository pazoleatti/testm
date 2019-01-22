package com.aplana.sbrf.taxaccounting;

import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.ConfigurationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractScriptClass {
    protected static final Log LOG = LogFactory.getLog(AbstractScriptClass.class);

    public Logger logger;
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
        this.logger = (Logger) getSafeProperty("logger");
        this.formDataEvent = (FormDataEvent) getSafeProperty("formDataEvent");
        this.declarationService = (DeclarationService) getSafeProperty("declarationService");
        this.configurationService = (ConfigurationService) getSafeProperty("configurationService");
        this.userInfo = (TAUserInfo) getSafeProperty("userInfo");

    }

    protected Object getSafeProperty(String propertyName) {
        return scriptClass.getBinding().hasVariable(propertyName) ? scriptClass.getProperty(propertyName) : null;
    }

    public abstract void run();

    protected void initConfiguration() {
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
