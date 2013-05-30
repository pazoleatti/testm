package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author ibukanov
 * Класс используется для поиска данных по журналу аудита
 */
public class LogSystemFilter implements Serializable{
	private static final long serialVersionUID = 1L;

	private int userId;
	private int reportPeriodId;
	private int formKindId;
	private int formTypeId;
	private int DeclarationTypeId;
	private int departmentId;
	private Date fromSearchDate;
	private Date toSearchDate;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(int reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public int getFormKindId() {
		return formKindId;
	}

	public void setFormKindId(int formKindId) {
		this.formKindId = formKindId;
	}

	public int getFormTypeId() {
		return formTypeId;
	}

	public void setFormTypeId(int formTypeId) {
		this.formTypeId = formTypeId;
	}

	public int getDeclarationTypeId() {
		return DeclarationTypeId;
	}

	public void setDeclarationTypeId(int declarationTypeId) {
		DeclarationTypeId = declarationTypeId;
	}

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public Date getFromSearchDate() {
		return fromSearchDate;
	}

	public void setFromSearchDate(Date fromSearchDate) {
		this.fromSearchDate = fromSearchDate;
	}

	public Date getToSearchDate() {
		return toSearchDate;
	}

	public void setToSearchDate(Date toSearchDate) {
		this.toSearchDate = toSearchDate;
	}
}
