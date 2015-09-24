package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Проверка доступа к справочнику
 */
public class CheckRefBookAction extends UnsecuredActionImpl<CheckRefBookResult> implements ActionName {

    private long refBookId;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    @Override
    public String getName() {
        return "Проверка доступности справочника";
    }
}
