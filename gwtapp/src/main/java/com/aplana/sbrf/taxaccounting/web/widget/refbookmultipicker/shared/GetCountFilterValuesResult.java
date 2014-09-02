package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetCountFilterValuesResult implements Result {
    private static final long serialVersionUID = 1099548218534060155L;

    Integer count;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
