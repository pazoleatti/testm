package com.aplana.sbrf.taxaccounting.web.module.scriptExecution.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Stanislav Yasinskiy
 */
public class ScriptExecutionResult implements Result {
    private static final long serialVersionUID = 6123459328456273461L;

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}