package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;

/**
 * Модельный класс сумм по ставкам для формирования 6 НДФЛ
 */
public class NdflSumByRate {

    // Код дохода, для которого вычисляются дивиденты
    public static final String INCOME_CODE_DIV = "1010";

    // Ставка
    private Integer taxRate;
    // НачислДох
    private BigDecimal incomeAccruedSumm;
    // НачислДохДив
    private BigDecimal incomeAccruedSummDiv;
    // ВычетНал
    private BigDecimal totalDeductionsSumm;
    // ИсчислНал
    private Integer calculatedTax;
    // ИсчислНалДив
    private Integer calculatedTaxDiv;
    // АвансПлат
    private BigDecimal prepaymentSum;

    public void addNdflSumByRate(NdflSumByRate ndflSumByRate) {
        addIncomeAccruedSumm(ndflSumByRate.getIncomeAccruedSumm());
        addIncomeAccruedSummDiv(ndflSumByRate.getIncomeAccruedSummDiv());
        addTotalDeductionsSumm(ndflSumByRate.getTotalDeductionsSumm());
        addCalculatedTax(ndflSumByRate.getCalculatedTax());
        addCalculatedTaxDiv(ndflSumByRate.getCalculatedTaxDiv());
        addPrepaymentSum(ndflSumByRate.getPrepaymentSum());
    }

    public Integer getTaxRate() {
        return taxRate;
    }
    public void setTaxRate(Integer taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getIncomeAccruedSumm() {
        return incomeAccruedSumm;
    }
    public void setIncomeAccruedSumm(BigDecimal incomeAccruedSumm) {
        this.incomeAccruedSumm = incomeAccruedSumm;
    }
    public void addIncomeAccruedSumm(BigDecimal incomeAccruedSumm) {
        this.incomeAccruedSumm = this.incomeAccruedSumm == null ? incomeAccruedSumm :
                this.incomeAccruedSumm.add(incomeAccruedSumm);
    }

    public BigDecimal getIncomeAccruedSummDiv() {
        return incomeAccruedSummDiv;
    }
    public void setIncomeAccruedSummDiv(BigDecimal incomeAccruedSummDiv) {
        this.incomeAccruedSummDiv = incomeAccruedSummDiv;
    }
    public void addIncomeAccruedSummDiv(BigDecimal incomeAccruedSummDiv) {
        this.incomeAccruedSummDiv = this.incomeAccruedSummDiv == null ? incomeAccruedSummDiv :
                this.incomeAccruedSummDiv.add(incomeAccruedSummDiv);
    }

    public BigDecimal getTotalDeductionsSumm() {
        return totalDeductionsSumm;
    }
    public void setTotalDeductionsSumm(BigDecimal totalDeductionsSumm) {
        this.totalDeductionsSumm = totalDeductionsSumm;
    }
    public void addTotalDeductionsSumm(BigDecimal totalDeductionsSumm) {
        this.totalDeductionsSumm = this.totalDeductionsSumm == null ? totalDeductionsSumm :
                this.totalDeductionsSumm.add(totalDeductionsSumm);
    }

    public Integer getCalculatedTax() {
        return calculatedTax;
    }
    public void setCalculatedTax(Integer calculatedTax) {
        this.calculatedTax = calculatedTax;
    }
    public void addCalculatedTax(Integer calculatedTax) {
        this.calculatedTax = this.calculatedTax == null ? calculatedTax :
                this.calculatedTax + calculatedTax;
    }

    public Integer getCalculatedTaxDiv() {
        return calculatedTaxDiv;
    }
    public void setCalculatedTaxDiv(Integer calculatedTaxDiv) {
        this.calculatedTaxDiv = calculatedTaxDiv;
    }
    public void addCalculatedTaxDiv(Integer calculatedTaxDiv) {
        this.calculatedTaxDiv = this.calculatedTaxDiv == null ? calculatedTaxDiv :
                this.calculatedTaxDiv + calculatedTaxDiv;
    }

    public BigDecimal getPrepaymentSum() {
        return prepaymentSum;
    }
    public void setPrepaymentSum(BigDecimal prepaymentSum) {
        this.prepaymentSum = prepaymentSum;
    }
    public void addPrepaymentSum(BigDecimal prepaymentSum) {
        this.prepaymentSum = this.prepaymentSum == null ? prepaymentSum :
                this.prepaymentSum.add(prepaymentSum);
    }
}
