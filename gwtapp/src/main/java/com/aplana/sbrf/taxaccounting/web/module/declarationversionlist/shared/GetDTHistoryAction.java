package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetDTHistoryAction extends UnsecuredActionImpl<GetDTHistoryResult> {
    private int typeId;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
