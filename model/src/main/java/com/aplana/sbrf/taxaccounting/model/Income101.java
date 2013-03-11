package com.aplana.sbrf.taxaccounting.model;

/*
 * Оборотная ведомость (Форма 0409101-СБ)
 */
public class Income101 {
	// id
	private Integer id;
	// Идентификатор отчетного периода
	private Integer reportPeriodId;
	// Номер счета
	private String Account;
	// Входящие остатки по дебету
	private Double IncomeDebetRemains;
	// Входящие остатки по кредиту
	private Double IncomeCreditRemains;
	// Обороты по дебету
	private Double DebetRate;
	// Обороты по кредиту
	private Double CreditRate;
	// Исходящие остатки по дебету
	private Double OutcomeDebetRemains;
	// Исходящие остатки по кредиту
	private Double OutcomeCreditRemains;
	
	public Integer getReportPeriodId() {
		return reportPeriodId;
	}
	public void setReportPeriodId(Integer reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}
	public String getAccount() {
		return Account;
	}
	public void setAccount(String account) {
		Account = account;
	}
	public Double getIncomeDebetRemains() {
		return IncomeDebetRemains;
	}
	public void setIncomeDebetRemains(Double incomeDebetRemains) {
		IncomeDebetRemains = incomeDebetRemains;
	}
	public Double getIncomeCreditRemains() {
		return IncomeCreditRemains;
	}
	public void setIncomeCreditRemains(Double incomeCreditRemains) {
		IncomeCreditRemains = incomeCreditRemains;
	}
	public Double getDebetRate() {
		return DebetRate;
	}
	public void setDebetRate(Double debetRate) {
		DebetRate = debetRate;
	}
	public Double getCreditRate() {
		return CreditRate;
	}
	public void setCreditRate(Double creditRate) {
		CreditRate = creditRate;
	}
	public Double getOutcomeDebetRemains() {
		return OutcomeDebetRemains;
	}
	public void setOutcomeDebetRemains(Double outcomeDebetRemains) {
		OutcomeDebetRemains = outcomeDebetRemains;
	}
	public Double getOutcomeCreditRemains() {
		return OutcomeCreditRemains;
	}
	public void setOutcomeCreditRemains(Double outcomeCreditRemains) {
		OutcomeCreditRemains = outcomeCreditRemains;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
}
