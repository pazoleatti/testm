package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * User: ibukanov
 * Модельный класс к журналу аудита, возвращаемый клиенту как поисковый резултат
 */
public class LogSearchResultItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private Date logDate;
	private String ip;
	private FormDataEvent event;
	private String user;
	private String roles;
    private String departmentName;
	private String declarationTypeName;
	private String formTypeName;
	private FormDataKind formKind;
	private String note;
	private String userDepartmentName;
    private String reportPeriodName;
    private String logId;
    private Integer formTypeId;
    private AuditFormType auditFormType;
    private Integer cnt;
    private String server;

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public LogSearchResultItem() {
    }

    public String getReportPeriodName() {
        return reportPeriodName;
    }

    public void setReportPeriodName(String reportPeriodName) {
        this.reportPeriodName = reportPeriodName;
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getLogDate() {
		return logDate;
	}

	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public FormDataEvent getEvent() {
		return event;
	}

	public void setEvent(FormDataEvent eventId) {
		this.event = eventId;
	}

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDeclarationTypeName() {
        return declarationTypeName;
    }

    public void setDeclarationTypeName(String declarationTypeName) {
        this.declarationTypeName = declarationTypeName;
    }

    public String getFormTypeName() {
        return formTypeName;
    }

    public void setFormTypeName(String formTypeName) {
        this.formTypeName = formTypeName;
    }

	public FormDataKind getFormKind() {
		return formKind;
	}

	public void setFormKind(FormDataKind formKind) {
		this.formKind = formKind;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

    public String getUserDepartmentName() {
        return userDepartmentName;
    }

    public void setUserDepartmentName(String userDepartmentName) {
        this.userDepartmentName = userDepartmentName;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public AuditFormType getAuditFormType() {
        return auditFormType;
    }

    public void setAuditFormType(AuditFormType auditFormType) {
        this.auditFormType = auditFormType;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return ("id: " + id) + ", user: " + user + " ip: " + ip +
                " department: " + " departmentName: " + departmentName +
                " roles: " + roles + " event: " + (event != null ? event.getTitle() : "") + " logdate: " + logDate +
                " note: " + note + " reportperiod: " +
                " formKind: " + (formKind != null ? formKind.getTitle() : "") + " formTypeName: " + formTypeName + " declarationTypeName: " + declarationTypeName;
    }
}
