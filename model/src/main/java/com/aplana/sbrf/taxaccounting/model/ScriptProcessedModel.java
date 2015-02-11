package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модель для данных, обрабатываемых скриптом
 * Используется так же для определения, были ли данные обработаны скриптом
 * @author dloshkarev
 */
public class ScriptProcessedModel implements Serializable {
    private static final long serialVersionUID = 6692415856020465402L;

    /** Признак того, что данные обработаны скриптом */
    Boolean processedByScript;

    public Boolean isProcessedByScript() {
        return processedByScript;
    }

    public void setProcessedByScript(Boolean processedByScript) {
        this.processedByScript = processedByScript;
    }
}
