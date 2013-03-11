package com.aplana.sbrf.taxaccounting.model;

/*
 * Оборотная ведомость (Форма 0409102-СБ)
 */
public class Income102 {

	// Идентификатор отчетного периода
	private Integer reportPeriodId;
	// Код ОПУ
	private String opuCode;
	// Сумма
	private Double totalSum;

	public Integer getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(Integer reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public String getOpuCode() {
		return opuCode;
	}

	public void setOpuCode(String opuCode) {
		this.opuCode = opuCode;
	}

	public Double getTotalSum() {
		return totalSum;
	}

	public void setTotalSum(Double totalSum) {
		this.totalSum = totalSum;
	}
}
