package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Модель отчетного периода для подразделений
 */
public class DepartmentReportPeriod implements Serializable, SecuredEntity {
	private static final long serialVersionUID = 5623552659772659276L;

	/**
	 * Идентификатор
	 */
    private Integer id;

	/**
	 * Отчетный период
	 */
	private ReportPeriod reportPeriod;

	/**
	 * Подразделение, к которому привязан отчетный период
	 */
	private Integer departmentId;

	/**
	 * Активность периода.
	 */
	private boolean isActive;

	/**
	 * Дата сдачи корректировки
	 */
    private Date correctionDate;

	/**
	 * Права доступа
	 */
	private long permissions;

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public boolean isActive() {
		return isActive;
	}

	public ReportPeriod getReportPeriod() {
		return reportPeriod;
	}

	public void setReportPeriod(ReportPeriod reportPeriod) {
		this.reportPeriod = reportPeriod;
	}

	public void setCorrectionDate(Date correctionDate) {
		this.correctionDate = correctionDate;
	}

	public Date getCorrectionDate() {
		return correctionDate;
	}

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

	/**
	 * Формирует сообщение о корректировочном периоде
	 * @param formatExp	шаблон форматирования даты
	 * @return строка с сообщением о корректировочномм приоде, если период не корректировочный возвращается пустая
	 * строка
	 */
	public String createCorrPeriodMessage(String formatExp) {
		String result = "";
		SimpleDateFormat formatter = new SimpleDateFormat(formatExp);
		if (this.getCorrectionDate() != null) {
			result = ", с датой сдачи корректировки " + formatter.format(this.getCorrectionDate());
		}
		return result;
	}
}
