package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.aplana.sbrf.taxaccounting.model.NotificationType;

import java.io.Serializable;
import java.util.Date;

public class NotificationTableRow implements Serializable {
	private static final long serialVersionUID = 4077680439794472365L;

    private Long id;
    private Date date;
    private String msg;
    private Boolean canDelete;
    private String blobDataId;
    private String reportId;
    private NotificationType notificationType = NotificationType.DEFAULT;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(Boolean canDelete) {
        this.canDelete = canDelete;
    }

    public String getBlobDataId() {
        return blobDataId;
    }

    public void setBlobDataId(String blobDataId) {
        this.blobDataId = blobDataId;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
}
