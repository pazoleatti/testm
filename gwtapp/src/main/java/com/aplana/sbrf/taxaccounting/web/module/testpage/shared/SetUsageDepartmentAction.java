package com.aplana.sbrf.taxaccounting.web.module.testpage.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class SetUsageDepartmentAction extends UnsecuredActionImpl<SetUsageDepartmentResult> {

    private long departmentId;
    private boolean used;

    public SetUsageDepartmentAction() {
    }

    public SetUsageDepartmentAction(long departmentId, boolean used) {
        this.departmentId = departmentId;
        this.used = used;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
