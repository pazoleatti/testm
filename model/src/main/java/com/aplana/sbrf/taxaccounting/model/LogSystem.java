package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * User: ibukanov
 * Date: 30.05.13
 * Модельный класс к журналу аудита АС "Учёт налогов"
 */
public class LogSystem implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private Date logDate;
	private String ip;
	private int eventId;
	private int userId;
	private String roles;
	private String departmentName;
    private String reportPeriodName;
	private Integer declarationTypeId;
	private Integer formTypeId;
	private Integer formKindId;
	private String note;
    private String userDepartmentName;

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

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
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

    public Integer getDeclarationTypeId() {
		return declarationTypeId;
	}

	public void setDeclarationTypeId(Integer declarationTypeId) {
		this.declarationTypeId = declarationTypeId;
	}

	public Integer getFormTypeId() {
		return formTypeId;
	}

	public void setFormTypeId(Integer formTypeId) {
		this.formTypeId = formTypeId;
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
}
