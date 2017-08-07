package com.aplana.sbrf.taxaccounting.web.module.commonparameter.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetUserRoleCommonParameterResult implements Result {

    private boolean canEdit;

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}
