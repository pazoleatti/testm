package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class DTDeleteResult implements Result {
    private String logEntriesUuid;

    public String getLogEntriesUuid() {
        return logEntriesUuid;
    }

    public void setLogEntriesUuid(String logEntriesUuid) {
        this.logEntriesUuid = logEntriesUuid;
    }
}
