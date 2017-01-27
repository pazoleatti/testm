package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;

/**
 * Суммы по ставкам для формирования 6 НДФЛ
 */
public class NdflPersonIncomeByRate {

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
    private Long calculatedTax;
    // ИсчислНалДив
    private Long calculatedTaxDiv;
    // АвансПлат
    private Long prepaymentSum;

    /**
     * Суммирование в рамках конкретной ставки
     * @param ndflPersonIncomeByRate
     */
    public void addNdflSumByRate(NdflPersonIncomeByRate ndflPersonIncomeByRate) {
        addIncomeAccruedSumm(ndflPersonIncomeByRate.getIncomeAccruedSumm());
        addIncomeAccruedSummDiv(ndflPersonIncomeByRate.getIncomeAccruedSummDiv());
        addTotalDeductionsSumm(ndflPersonIncomeByRate.getTotalDeductionsSumm());
        addCalculatedTax(ndflPersonIncomeByRate.getCalculatedTax());
        addCalculatedTaxDiv(ndflPersonIncomeByRate.getCalculatedTaxDiv());
        addPrepaymentSum(ndflPersonIncomeByRate.getPrepaymentSum());
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

    public Long getCalculatedTax() {
        return calculatedTax;
    }
    public void setCalculatedTax(Long calculatedTax) {
        this.calculatedTax = calculatedTax;
    }
    public void addCalculatedTax(Long calculatedTax) {
        this.calculatedTax = this.calculatedTax == null ? calculatedTax :
                this.calculatedTax + calculatedTax;
    }

    public Long getCalculatedTaxDiv() {
        return calculatedTaxDiv;
    }
    public void setCalculatedTaxDiv(Long calculatedTaxDiv) {
        this.calculatedTaxDiv = calculatedTaxDiv;
    }
    public void addCalculatedTaxDiv(Long calculatedTaxDiv) {
        this.calculatedTaxDiv = this.calculatedTaxDiv == null ? calculatedTaxDiv :
                this.calculatedTaxDiv + calculatedTaxDiv;
    }

    public Long getPrepaymentSum() {
        return prepaymentSum;
    }
    public void setPrepaymentSum(Long prepaymentSum) {
        this.prepaymentSum = prepaymentSum;
    }
    public void addPrepaymentSum(Long prepaymentSum) {
        this.prepaymentSum = this.prepaymentSum == null ? prepaymentSum :
                this.prepaymentSum + prepaymentSum;
    }
}
