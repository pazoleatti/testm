package com.aplana.sbrf.taxaccounting.web.module.testpage.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class DoEventAction extends UnsecuredActionImpl<DoEventResult> {

    private String code;

    public DoEventAction() {
    }

    public DoEventAction(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
