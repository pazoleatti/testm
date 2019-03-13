package com.aplana.sbrf.taxaccounting.permissions;

import com.aplana.sbrf.taxaccounting.dao.api.NotificationDao;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.userdetails.User;

/**
 * Права доступа при работе с оповещениями
 */
@Configurable
public abstract class NotificationPermission extends AbstractPermission<Notification> {

    @Autowired
    protected NotificationDao notificationDao;

    /**
     * Право наскачивание файла оповещения
     */
    public static final Permission<Notification> DOWNLOAD_NOTIFICATION_FILE = new DownloadNotificationFile(1 << 0);

    public NotificationPermission(long mask) {
        super(mask);
    }

    public static final class DownloadNotificationFile extends NotificationPermission{
        public DownloadNotificationFile(long mask) {
            super(mask);
        }

        @Override
        protected boolean isGrantedInternal(User user, Notification targetDomainObject, Logger logger) {
            return notificationDao.existsByUserIdAndReportId(targetDomainObject.getUserId(), targetDomainObject.getReportId());
        }
    }
}
