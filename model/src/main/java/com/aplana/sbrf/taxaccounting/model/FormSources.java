package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Источники-приемники нф, получаемые в скрипте
 *
 * (Andrey Drunk) кроме источников-форм, данный класс может использоваться для описания источников-деклараций
 *
 * @author dloshkarev
 */
public class FormSources implements Serializable {
    private static final long serialVersionUID = -271358527792210291L;
    /** Источники-приемники */
    List<Relation> sourceList;
    /** Признак того, что источники-приемники были получены скриптом */
    Boolean sourcesProcessedByScript;

    public List<Relation> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<Relation> sourceList) {
        this.sourceList = sourceList;
    }

    public Boolean isSourcesProcessedByScript() {
        return sourcesProcessedByScript;
    }

    public void setSourcesProcessedByScript(Boolean sourcesProcessedByScript) {
        this.sourcesProcessedByScript = sourcesProcessedByScript;
    }
}
