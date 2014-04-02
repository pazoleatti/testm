package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Fail Mukhametdinov
 */
public abstract class AbstractRefBookScriptResult implements Result {
    private static final long serialVersionUID = 2215037357471511029L;
    private String script;

    private String name;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}