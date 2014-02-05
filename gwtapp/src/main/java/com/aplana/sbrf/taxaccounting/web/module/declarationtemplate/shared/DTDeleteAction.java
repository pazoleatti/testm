package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class DTDeleteAction extends UnsecuredActionImpl<DTDeleteResult> {
    private int dtTypeId;

    public int getDtTypeId() {
        return dtTypeId;
    }

    public void setDtTypeId(int dtTypeId) {
        this.dtTypeId = dtTypeId;
    }
}
