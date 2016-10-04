package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Проверка доступа к справочнику
 */
public class CheckRefBookAction extends UnsecuredActionImpl<CheckRefBookResult> implements ActionName {

    private long refBookId;
    private RefBookType typeForCheck;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    public RefBookType getTypeForCheck() {
        return typeForCheck;
    }

    public void setTypeForCheck(RefBookType typeForCheck) {
        this.typeForCheck = typeForCheck;
    }

    @Override
    public String getName() {
        return "Проверка доступности справочника";
    }
}
