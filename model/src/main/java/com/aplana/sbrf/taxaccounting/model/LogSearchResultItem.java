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
	private TAUser user;
	private String roles;
	private Department department;
    private String departmentName;
	private ReportPeriod reportPeriod;
	private DeclarationType declarationType;
	private FormType formType;
	private FormDataKind formKind;
	private String note;
	private Department userDepartment;
    private String userDepartmentHierarchy;

    public String getUserDepartmentHierarchy() {
        return userDepartmentHierarchy;
    }

    public void setUserDepartmentHierarchy(String userDepartmentHierarchy) {
        this.userDepartmentHierarchy = userDepartmentHierarchy;
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

	public TAUser getUser() {
		return user;
	}

	public void setUser(TAUser user) {
		this.user = user;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public ReportPeriod getReportPeriod() {
		return reportPeriod;
	}

	public void setReportPeriod(ReportPeriod reportPeriod) {
		this.reportPeriod = reportPeriod;
	}

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	public void setDeclarationType(DeclarationType declarationType) {
		this.declarationType = declarationType;
	}

	public FormType getFormType() {
		return formType;
	}

	public void setFormType(FormType formType) {
		this.formType = formType;
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

	public Department getUserDepartment() {
		return userDepartment;
	}

	public void setUserDepartment(Department userDepartment) {
		this.userDepartment = userDepartment;
	}

    @Override
    public String toString() {
        return ("id: " + id) + ", user: " + (user != null ? user.getLogin() : "") + " ip: " + ip +
                " department: " + (department != null ? department.getName() : "") + " departmentName: " + departmentName +
                " roles: " + roles + " event: " + (event != null ? event.getTitle() : "") + " logdate: " + logDate +
                " note: " + note + " reportperiod: " + (reportPeriod != null ? reportPeriod.getName() : "") +
                " formKind: " + (formKind != null ? formKind.getName() : "");
    }
}
