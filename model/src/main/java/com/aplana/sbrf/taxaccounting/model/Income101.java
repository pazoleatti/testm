package com.aplana.sbrf.taxaccounting.model;

/*
 * Оборотная ведомость (Форма 0409101-СБ)
 */
public class Income101 {

	// Идентификатор отчетного периода
	private Integer reportPeriodId;
	// Номер счета
	private String account;
	// Входящие остатки по дебету
	private Double incomeDebetRemains;
	// Входящие остатки по кредиту
	private Double incomeCreditRemains;
	// Обороты по дебету
	private Double debetRate;
	// Обороты по кредиту
	private Double creditRate;
	// Исходящие остатки по дебету
	private Double outcomeDebetRemains;
	// Исходящие остатки по кредиту
	private Double outcomeCreditRemains;
	
	public Integer getReportPeriodId() {
		return reportPeriodId;
	}
	public void setReportPeriodId(Integer reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	
	public Double getIncomeDebetRemains() {
		return incomeDebetRemains;
	}
	public void setIncomeDebetRemains(Double incomeDebetRemains) {
		this.incomeDebetRemains = incomeDebetRemains;
	}
	public Double getIncomeCreditRemains() {
		return incomeCreditRemains;
	}
	public void setIncomeCreditRemains(Double incomeCreditRemains) {
		this.incomeCreditRemains = incomeCreditRemains;
	}
	public Double getDebetRate() {
		return debetRate;
	}
	public void setDebetRate(Double debetRate) {
		this.debetRate = debetRate;
	}
	public Double getCreditRate() {
		return creditRate;
	}
	public void setCreditRate(Double creditRate) {
		this.creditRate = creditRate;
	}
	public Double getOutcomeDebetRemains() {
		return outcomeDebetRemains;
	}
	public void setOutcomeDebetRemains(Double outcomeDebetRemains) {
		this.outcomeDebetRemains = outcomeDebetRemains;
	}
	public Double getOutcomeCreditRemains() {
		return outcomeCreditRemains;
	}
	public void setOutcomeCreditRemains(Double outcomeCreditRemains) {
		this.outcomeCreditRemains = outcomeCreditRemains;
	}
}
