package com.aplana.sbrf.taxaccounting.web.module.testpage2.shared;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDataResult implements Result {

    private List<TAUser> values;

    public List<TAUser> getValues() {
        return values;
    }

    public void setValues(List<TAUser> values) {
        this.values = values;
    }
}
