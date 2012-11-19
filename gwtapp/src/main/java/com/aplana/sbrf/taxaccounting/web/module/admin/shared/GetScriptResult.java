package com.aplana.sbrf.taxaccounting.web.module.admin.shared;

import com.aplana.sbrf.taxaccounting.model.Script;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Vitalii Samolovskikh
 */
public class GetScriptResult implements Result {
    private Script script;

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
}
