package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.Result;

public class RemovePeriodResult implements Result {
	private static final long serialVersionUID = -2300921702070767267L;

	private String uuid;

    private boolean hasFatalErrors = false;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

    public boolean isHasFatalErrors() {
        return hasFatalErrors;
    }

    public void setHasFatalErrors(boolean hasFatalErrors) {
        this.hasFatalErrors = hasFatalErrors;
    }
}
