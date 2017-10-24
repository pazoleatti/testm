package com.aplana.sbrf.taxaccounting;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

public abstract class AbstractScriptClass {
    protected Logger logger;
    protected FormDataEvent formDataEvent;
    protected groovy.lang.Script scriptClass;
    protected TAUserInfo userInfo;

    protected boolean showTiming;

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
        if (scriptClass.getBinding().hasVariable("userInfo")) {
            this.userInfo = (TAUserInfo) scriptClass.getProperty("userInfo");
        }
    }

    public abstract void run();

    protected void logForDebug(String message, Object... args) {
        if (showTiming) {
            logger.info(message, args);
        }
    }
}
