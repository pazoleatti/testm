package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckRefBookResult implements Result {
    private static final long serialVersionUID = 6657849317878384244L;

    private boolean available;
    private boolean versioned;
    private boolean uploadAvailable;
    private boolean scriptStatus;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setVersioned(boolean versioned) {
        this.versioned = versioned;
    }

    public boolean isVersioned() {
        return versioned;
    }

    public boolean isUploadAvailable() {
        return uploadAvailable;
    }

    public void setUploadAvailable(boolean uploadAvailable) {
        this.uploadAvailable = uploadAvailable;
    }

    public boolean isScriptStatus() {
        return scriptStatus;
    }

    public void setScriptStatus(boolean scriptStatus) {
        this.scriptStatus = scriptStatus;
    }
}
