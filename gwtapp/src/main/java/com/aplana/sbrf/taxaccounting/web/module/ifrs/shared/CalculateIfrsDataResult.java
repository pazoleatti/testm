package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * @author lhaziev
 */
public class CalculateIfrsDataResult implements Result {

    // uuid отчетности для МСФО
    private String blobDataId;
    private String uuid;

    public String getBlobDataId() {
        return blobDataId;
    }

    public void setBlobDataId(String blobDataId) {
        this.blobDataId = blobDataId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
