package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetTableAttributesResult implements Result {
    List<RefBookAttribute> attributes;

    public List<RefBookAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<RefBookAttribute> attributes) {
        this.attributes = attributes;
    }
}
