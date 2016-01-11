package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.gwtplatform.dispatch.shared.Result;

public class CheckReportNotificationResult implements Result {
    private static final long serialVersionUID = 1651643421646415131L;

    private boolean exist;
    private String msg;
    private NotificationType notificationType;
    private String reportId;

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}
