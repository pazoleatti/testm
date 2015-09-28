package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Проверка линейности справочника
 */
public class CheckLinearAction extends UnsecuredActionImpl<CheckLinearResult> {
    private long refBookId;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }
}
