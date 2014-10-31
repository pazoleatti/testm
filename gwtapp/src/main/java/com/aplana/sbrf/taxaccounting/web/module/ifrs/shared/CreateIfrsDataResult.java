package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author lhaziev
 */
public class CreateIfrsDataResult implements Result {

    private String uuid;
    private boolean isError = false;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }
}
