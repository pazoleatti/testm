package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Модель отчетного периода для подразделений
 */
public class DepartmentReportPeriod implements Serializable, SecuredEntity {
	private static final long serialVersionUID = 5623552659772659276L;

	/**
	 * Идентификатор
	 */
	@Getter
	@Setter
    private Integer id;

	/**
	 * Отчетный период
	 */
	@Getter
	@Setter
	private ReportPeriod reportPeriod;

	/**
	 * Подразделение, к которому привязан отчетный период
	 */
	@Getter
	@Setter
	private Integer departmentId;

	/**
	 * Активность периода.
	 */
	@Getter
	private boolean isActive;

	/**
	 * Дата сдачи корректировки
	 */
	@Getter
	@Setter
    private Date correctionDate;

	/**
	 * Права доступа
	 */
	private long permissions;

	@Override
	public long getPermissions() {
		return permissions;
	}

	@Override
	public void setPermissions(long permissions) {
		this.permissions = permissions;
	}

	public void setIsActive(boolean active) {
		this.isActive = active;
	}

	@Override
	public String toString() {
		return "DepartmentReportPeriod{" +
				"id=" + id +
				", reportPeriod=" + reportPeriod +
				", departmentId=" + departmentId +
				", isActive=" + isActive +
				", correctionDate=" + correctionDate +
				", permissions=" + permissions +
				'}';
	}
}
