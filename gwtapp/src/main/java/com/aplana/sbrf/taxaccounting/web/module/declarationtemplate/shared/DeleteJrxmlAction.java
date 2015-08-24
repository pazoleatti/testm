package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class DeleteJrxmlAction extends UnsecuredActionImpl<DeleteJrxmlResult> {
    private int dtId;

    public int getDtId() {
        return dtId;
    }

    public void setDtId(int dtId) {
        this.dtId = dtId;
    }
}
