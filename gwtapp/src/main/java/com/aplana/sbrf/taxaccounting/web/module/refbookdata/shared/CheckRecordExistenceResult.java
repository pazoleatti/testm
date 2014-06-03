package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * @author lhaziev
 */
public class CheckRecordExistenceResult implements Result {

    private boolean recordExistence = false;

    public boolean isRecordExistence() {
        return recordExistence;
    }

    public void setRecordExistence(boolean recordExistence) {
        this.recordExistence = recordExistence;
    }
}
