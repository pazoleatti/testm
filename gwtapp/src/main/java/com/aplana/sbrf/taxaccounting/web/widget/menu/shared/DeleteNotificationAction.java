package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * Удаление оповещений. Также используется для предварительной проверки возможности удаления всех оповещений текущим пользователем
 * @author dloshkarev
 */
public class DeleteNotificationAction extends UnsecuredActionImpl<DeleteNotificationResult> {
    private List<Long> notificationIds;
    private boolean deleteWithoutCheck;

    public boolean isDeleteWithoutCheck() {
        return deleteWithoutCheck;
    }

    public void setDeleteWithoutCheck(boolean deleteWithoutCheck) {
        this.deleteWithoutCheck = deleteWithoutCheck;
    }

    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(List<Long> notificationIds) {
        this.notificationIds = notificationIds;
    }
}
