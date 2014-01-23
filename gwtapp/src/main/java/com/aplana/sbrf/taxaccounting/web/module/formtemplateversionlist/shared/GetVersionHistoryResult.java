package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetVersionHistoryResult implements Result {
    private List<TemplateChangesExt> changeses;

    public List<TemplateChangesExt> getChangeses() {
        return changeses;
    }

    public void setChangeses(List<TemplateChangesExt> changeses) {
        this.changeses = changeses;
    }
}
