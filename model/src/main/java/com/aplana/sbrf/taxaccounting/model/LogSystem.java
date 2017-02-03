package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * User: ibukanov
 * Date: 30.05.13
 * Модельный класс к журналу аудита АС "Учёт налогов"
 */
public class LogSystem implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private String ip;
	private int eventId;
    private String userLogin;
	private String roles;
	private String formDepartmentName;
    private String reportPeriodName;
	private String declarationTypeName;
	private String formTypeName;
	private Integer formKindId;
	private String note;
    private String userDepartmentName;
    private Integer formDepartmentId;
    private String logId;
    private Integer formTypeId;
    private Integer auditFormTypeId;
    private String server;

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

    public String getRoles() {
        return roles;
    }

	public void setRoles(String roles) {
		this.roles = roles;
	}

    public String getFormDepartmentName() {
        return formDepartmentName;
    }

    public void setFormDepartmentName(String formDepartmentName) {
        this.formDepartmentName = formDepartmentName;
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

	public Integer getFormKindId() {
		return formKindId;
	}

	public void setFormKindId(Integer formKindId) {
		this.formKindId = formKindId;
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

    public Integer getFormDepartmentId() {
        return formDepartmentId;
    }

    public void setFormDepartmentId(Integer formDepartmentId) {
        this.formDepartmentId = formDepartmentId;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public Integer getAuditFormTypeId() {
        return auditFormTypeId;
    }

    public void setAuditFormTypeId(Integer auditFormTypeId) {
        this.auditFormTypeId = auditFormTypeId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
