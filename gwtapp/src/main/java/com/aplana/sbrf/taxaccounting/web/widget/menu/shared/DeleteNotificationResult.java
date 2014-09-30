package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class DeleteNotificationResult implements Result {
    private static final long serialVersionUID = 9159630412727838764L;

    /** Список оповещений, разрешенных для удаления текущим пользователем */
    private List<Long> allowedNotifications;

    public List<Long> getAllowedNotifications() {
        return allowedNotifications;
    }

    public void setAllowedNotifications(List<Long> allowedNotifications) {
        this.allowedNotifications = allowedNotifications;
    }
}
