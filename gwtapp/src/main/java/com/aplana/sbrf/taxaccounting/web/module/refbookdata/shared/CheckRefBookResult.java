package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckRefBookResult implements Result {
    private static final long serialVersionUID = 6657849317878384244L;

    private boolean available;
    private boolean versioned;

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
}
