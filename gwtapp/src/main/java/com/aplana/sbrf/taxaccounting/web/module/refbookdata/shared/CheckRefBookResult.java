package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class CheckRefBookResult implements Result {
    private static final long serialVersionUID = 6657849317878384244L;

    private boolean available;
    private boolean versioned;
    private boolean uploadAvailable;
    private Map<FormDataEvent, Boolean> eventScriptStatus;

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

    public Map<FormDataEvent, Boolean> getEventScriptStatus() {
        return eventScriptStatus;
    }

    public void setEventScriptStatus(Map<FormDataEvent, Boolean> eventScriptStatus) {
        this.eventScriptStatus = eventScriptStatus;
    }
}
