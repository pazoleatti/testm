package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Модельный класс сумм по датам для формирования 6 НДФЛ
 */
public class NdflSumByDate {

    // Дата начисления дохода
    private Date incomeAccruedDate;

    // Дата налога
    private Date taxDate;

    // Срок перечисления налога в бюджет
    private Date taxTransferDate;

    // Сумма выплаченного дохода
    private BigDecimal incomePayoutSumm;

    // Сумма налога удержанная
    private Integer withholdingTax;

    public Date getIncomeAccruedDate() {
        return incomeAccruedDate;
    }
    public void setIncomeAccruedDate(Date incomeAccruedDate) {
        this.incomeAccruedDate = incomeAccruedDate;
    }

    public Date getTaxDate() {
        return taxDate;
    }
    public void setTaxDate(Date taxDate) {
        this.taxDate = taxDate;
    }

    public Date getTaxTransferDate() {
        return taxTransferDate;
    }
    public void setTaxTransferDate(Date taxTransferDate) {
        this.taxTransferDate = taxTransferDate;
    }

    public BigDecimal getIncomePayoutSumm() {
        return incomePayoutSumm;
    }
    public void setIncomePayoutSumm(BigDecimal incomePayoutSumm) {
        this.incomePayoutSumm = incomePayoutSumm;
    }
    public void addIncomePayoutSumm(BigDecimal incomePayoutSumm) {
        this.incomePayoutSumm.add(incomePayoutSumm);
    }

    public Integer getWithholdingTax() {
        return withholdingTax;
    }
    public void setWithholdingTax(Integer withholdingTax) {
        this.withholdingTax = withholdingTax;
    }
    public void addWithholdingTax(Integer withholdingTax) {
        this.withholdingTax += withholdingTax;
    }
}
