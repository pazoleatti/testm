package com.aplana.sbrf.taxaccounting.web.module.scriptexecution.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.io.Serializable;

public class CheckRoleResult implements Result {
    private static final long serialVersionUID = 4703272021329220544L;

    private boolean result;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
