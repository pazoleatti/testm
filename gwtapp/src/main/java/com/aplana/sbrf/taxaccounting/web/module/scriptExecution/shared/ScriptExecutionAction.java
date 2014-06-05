package com.aplana.sbrf.taxaccounting.web.module.scriptExecution.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Stanislav Yasinskiy
 */
public class ScriptExecutionAction extends UnsecuredActionImpl<ScriptExecutionResult> {

    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}