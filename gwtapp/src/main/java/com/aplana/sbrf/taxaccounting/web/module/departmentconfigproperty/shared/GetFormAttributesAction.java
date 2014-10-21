package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormAttributesAction extends UnsecuredActionImpl<GetFormAttributesResult> {
    Long refBookId;
    Long tableRefBookId;

    public Long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public Long getTableRefBookId() {
        return tableRefBookId;
    }

    public void setTableRefBookId(Long tableRefBookId) {
        this.tableRefBookId = tableRefBookId;
    }
}
