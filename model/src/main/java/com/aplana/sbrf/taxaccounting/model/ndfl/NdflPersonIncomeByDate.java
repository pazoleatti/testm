package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Суммы по датам для формирования 6 НДФЛ
 */
public class NdflPersonIncomeByDate {

    // Дата начисления дохода
    private Date incomeAccruedDate;

    // Дата налога
    private Date taxDate;

    // Срок перечисления налога в бюджет
    private Date taxTransferDate;

    // Сумма выплаченного дохода
    private BigDecimal incomePayoutSumm;

    // Сумма налога удержанная
    private Long withholdingTax;

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
        if (incomePayoutSumm != null) {
            this.incomePayoutSumm = this.incomePayoutSumm == null ? incomePayoutSumm : this.incomePayoutSumm.add(incomePayoutSumm);
        }
    }

    public Long getWithholdingTax() {
        return withholdingTax;
    }
    public void setWithholdingTax(Long withholdingTax) {
        this.withholdingTax = withholdingTax;
    }
    public void addWithholdingTax(Long withholdingTax) {
        if (withholdingTax != null) {
            this.withholdingTax = this.withholdingTax == null ? withholdingTax : this.withholdingTax + withholdingTax;
        }
    }
}
