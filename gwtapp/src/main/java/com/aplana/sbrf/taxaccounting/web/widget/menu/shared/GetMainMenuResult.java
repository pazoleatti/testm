package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import java.util.Date;

public class GetMainMenuResult extends AbstractMenuResult {
    private int rolesHashCode;
    private Date lastNotificationDate;

    public int getRolesHashCode() {
        return rolesHashCode;
    }

    public void setRolesHashCode(int rolesHashCode) {
        this.rolesHashCode = rolesHashCode;
    }

    public Date getLastNotificationDate() {
        return lastNotificationDate;
    }

    public void setLastNotificationDate(Date lastNotificationDate) {
        this.lastNotificationDate = lastNotificationDate;
    }
}
