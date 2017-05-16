package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormAttributesAction extends UnsecuredActionImpl<GetFormAttributesResult> {
    Long refBookId;
    Long tableRefBookId;
    private TaxType taxType;

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

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}
