package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Источники-приемники нф, получаемые в скрипте
 * @author dloshkarev
 */
public class FormSources implements Serializable {
    private static final long serialVersionUID = -271358527792210291L;
    /** Источники-приемники */
    List<FormToFormRelation> sourceList;
    /** Признак того, что источники-приемники были получены скриптом */
    Boolean sourcesProcessedByScript;

    public List<FormToFormRelation> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<FormToFormRelation> sourceList) {
        this.sourceList = sourceList;
    }

    public Boolean isSourcesProcessedByScript() {
        return sourcesProcessedByScript;
    }

    public void setSourcesProcessedByScript(Boolean sourcesProcessedByScript) {
        this.sourcesProcessedByScript = sourcesProcessedByScript;
    }
}
