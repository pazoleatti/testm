package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class DetectUserRoleResult implements Result {
	private static final long serialVersionUID = -6037420163541321038L;

    private boolean isControl;

    public boolean isControl() {
        return isControl;
    }

    public void setControl(boolean isControl) {
        this.isControl = isControl;
    }
}
