package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class DetectUserRoleResult implements Result {
	private static final long serialVersionUID = -6037420163541321038L;

    private boolean isControl;

    private boolean hasRoleOperator;

    public boolean isControl() {
        return isControl;
    }

    public void setControl(boolean isControl) {
        this.isControl = isControl;
    }

    public boolean isHasRoleOperator() {
        return hasRoleOperator;
    }

    public void setHasRoleOperator(boolean hasRoleOperator) {
        this.hasRoleOperator = hasRoleOperator;
    }
}
