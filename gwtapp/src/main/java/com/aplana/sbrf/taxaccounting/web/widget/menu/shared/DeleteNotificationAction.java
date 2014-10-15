package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Удаление оповещений.
 * @author dloshkarev
 */
public class DeleteNotificationAction extends UnsecuredActionImpl<DeleteNotificationResult> {
    private List<Long> notificationIds;

    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(List<Long> notificationIds) {
        this.notificationIds = notificationIds;
    }
}
