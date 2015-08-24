package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class LoadAllResult implements Result {
    private String uuid;
    private boolean fileSizeLimit;
    private String dialogMsg;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isFileSizeLimit() {
        return fileSizeLimit;
    }

    public void setFileSizeLimit(boolean fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;
    }

    public String getDialogMsg() {
        return dialogMsg;
    }

    public void setDialogMsg(String dialogMsg) {
        this.dialogMsg = dialogMsg;
    }
}
