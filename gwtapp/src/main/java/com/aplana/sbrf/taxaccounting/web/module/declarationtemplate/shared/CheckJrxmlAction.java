package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Проверяет есть ли отчеты сыформированные по этой jrxml
 * User: avanteev
 */
public class CheckJrxmlAction extends UnsecuredActionImpl<CheckJrxmlResult> {
    private int dtId;

    public int getDtId() {
        return dtId;
    }

    public void setDtId(int dtId) {
        this.dtId = dtId;
    }
}
