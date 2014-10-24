package com.aplana.sbrf.taxaccounting.web.module.testpage.shared;

import com.gwtplatform.dispatch.shared.Result;

public class DoEventResult implements Result {
    private static final long serialVersionUID = 1837776652451421385L;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
