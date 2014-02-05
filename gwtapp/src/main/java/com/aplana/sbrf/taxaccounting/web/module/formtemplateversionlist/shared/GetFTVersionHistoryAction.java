package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetFTVersionHistoryAction extends UnsecuredActionImpl<GetFTVersionHistoryResult> {
    private int formTypeId;

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }
}
