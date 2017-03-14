package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetNotificationCountAction extends UnsecuredActionImpl<GetNotificationCountResult> {
    private int rolesHashCode;

    public int getRolesHashCode() {
        return rolesHashCode;
    }

    public void setRolesHashCode(int rolesHashCode) {
        this.rolesHashCode = rolesHashCode;
    }
}
