package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetTableAttributesAction extends UnsecuredActionImpl<GetTableAttributesResult> {
    Long refBookId;

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }
}
