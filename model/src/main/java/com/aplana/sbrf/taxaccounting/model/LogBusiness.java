package com.aplana.sbrf.taxaccounting.model;


import java.io.Serializable;
import java.util.Date;

/**
 * Модельный класс для хранения общей истории событий для налоговых форм и деклараций
 */
public class LogBusiness implements Serializable {
	private static final long serialVersionUID = 1L;

	private long id;
	private Date logDate;
	private int eventId;
	private int userId;
	private String roles;
	private Integer declarationId;
	private Integer formId;
	private String note;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getLogDate() {
		return logDate;
	}

	public void setLogDate(Date logDate) {
		this.logDate = logDate;
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

	public Integer getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(Integer declarationId) {
		this.declarationId = declarationId;
	}

	public Integer getFormId() {
		return formId;
	}

	public void setFormId(Integer formId) {
		this.formId = formId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
