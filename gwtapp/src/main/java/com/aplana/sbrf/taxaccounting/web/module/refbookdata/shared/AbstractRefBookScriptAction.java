package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Fail Mukhametdinov
 */
public abstract class AbstractRefBookScriptAction<R extends AbstractRefBookScriptResult> extends UnsecuredActionImpl<R> {
    private Long refBookId;

    private String script;

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}