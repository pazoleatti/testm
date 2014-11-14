package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.gwtplatform.dispatch.shared.Result;


public class InitRefBookMultiResult implements Result {
    private static final long serialVersionUID = 1099858218534060155L;

    private long refBookId;
    private List<RefBookAttribute> attributes;

    public long getRefBookId() {
        return refBookId;
    }

    public void setRefBookId(long refBookId) {
        this.refBookId = refBookId;
    }

    public List<RefBookAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<RefBookAttribute> attributes) {
        this.attributes = attributes;
    }
}
