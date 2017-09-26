package com.aplana.sbrf.taxaccounting;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

public abstract class AbstractScriptClass {
    protected Logger logger;
    protected FormDataEvent formDataEvent;
    protected groovy.lang.Script scriptClass;

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
    }

    public abstract void run();
}
