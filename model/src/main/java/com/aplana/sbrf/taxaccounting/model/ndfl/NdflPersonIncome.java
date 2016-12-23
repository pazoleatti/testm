package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Andrey Drunk
 */
public class NdflPersonIncome extends NdflPersonDetail {

    private String incomeCode;
    private String incomeType;
    private Date incomeAccruedDate;
    private Date incomePayoutDate;
    private BigDecimal incomeAccruedSumm;
    private BigDecimal incomePayoutSumm;
    private BigDecimal totalDeductionsSumm;
    private BigDecimal taxBase;
    private Integer taxRate;
    private Date taxDate;
    private Integer calculatedTax;
    private Integer withholdingTax;
    private Integer notHoldingTax;
    private Integer overholdingTax;
    private Integer refoundTax;
    private Date taxTransferDate;
    private Date paymentDate;
    private String paymentNumber;
    private Integer taxSumm;

    public static final String TABLE_NAME = "ndfl_person_income";
    public static final String SEQ = "seq_ndfl_person_income";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "income_code", "income_type",
            "income_accrued_date", "income_payout_date", "income_accrued_summ", "income_payout_summ", "total_deductions_summ",
            "tax_base", "tax_rate", "tax_date", "calculated_tax", "withholding_tax", "not_holding_tax", "overholding_tax",
            "refound_tax", "tax_transfer_date", "payment_date", "payment_number", "tax_summ"};

    public Object[] createPreparedStatementArgs() {
        return new Object[]{id, ndflPersonId, rowNum, incomeCode, incomeType, incomeAccruedDate, incomePayoutDate,
                incomeAccruedSumm, incomePayoutSumm, totalDeductionsSumm, taxBase, taxRate, taxDate, calculatedTax,
                withholdingTax, notHoldingTax, overholdingTax, refoundTax, taxTransferDate, paymentDate, paymentNumber, taxSumm};
    }

    public String getIncomeCode() {
        return incomeCode;
    }

    public void setIncomeCode(String incomeCode) {
        this.incomeCode = incomeCode;
    }

    public String getIncomeType() {
        return incomeType;
    }

    public void setIncomeType(String incomeType) {
        this.incomeType = incomeType;
    }

    public Date getIncomeAccruedDate() {
        return incomeAccruedDate;
    }

    public void setIncomeAccruedDate(Date incomeAccruedDate) {
        this.incomeAccruedDate = incomeAccruedDate;
    }

    public Date getIncomePayoutDate() {
        return incomePayoutDate;
    }

    public void setIncomePayoutDate(Date incomePayoutDate) {
        this.incomePayoutDate = incomePayoutDate;
    }

    public BigDecimal getIncomeAccruedSumm() {
        return incomeAccruedSumm;
    }

    public void setIncomeAccruedSumm(BigDecimal incomeAccruedSumm) {
        this.incomeAccruedSumm = incomeAccruedSumm;
    }

    public BigDecimal getIncomePayoutSumm() {
        return incomePayoutSumm;
    }

    public void setIncomePayoutSumm(BigDecimal incomePayoutSumm) {
        this.incomePayoutSumm = incomePayoutSumm;
    }

    public BigDecimal getTotalDeductionsSumm() {
        return totalDeductionsSumm;
    }

    public void setTotalDeductionsSumm(BigDecimal totalDeductionsSumm) {
        this.totalDeductionsSumm = totalDeductionsSumm;
    }

    public BigDecimal getTaxBase() {
        return taxBase;
    }

    public void setTaxBase(BigDecimal taxBase) {
        this.taxBase = taxBase;
    }

    public Integer getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Integer taxRate) {
        this.taxRate = taxRate;
    }

    public Date getTaxDate() {
        return taxDate;
    }

    public void setTaxDate(Date taxDate) {
        this.taxDate = taxDate;
    }

    public Integer getCalculatedTax() {
        return calculatedTax;
    }

    public void setCalculatedTax(Integer calculatedTax) {
        this.calculatedTax = calculatedTax;
    }

    public Integer getWithholdingTax() {
        return withholdingTax;
    }

    public void setWithholdingTax(Integer withholdingTax) {
        this.withholdingTax = withholdingTax;
    }

    public Integer getNotHoldingTax() {
        return notHoldingTax;
    }

    public void setNotHoldingTax(Integer notHoldingTax) {
        this.notHoldingTax = notHoldingTax;
    }

    public Integer getOverholdingTax() {
        return overholdingTax;
    }

    public void setOverholdingTax(Integer overholdingTax) {
        this.overholdingTax = overholdingTax;
    }

    public Integer getRefoundTax() {
        return refoundTax;
    }

    public void setRefoundTax(Integer refoundTax) {
        this.refoundTax = refoundTax;
    }

    public Date getTaxTransferDate() {
        return taxTransferDate;
    }

    public void setTaxTransferDate(Date taxTransferDate) {
        this.taxTransferDate = taxTransferDate;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public Integer getTaxSumm() {
        return taxSumm;
    }

    public void setTaxSumm(Integer taxSumm) {
        this.taxSumm = taxSumm;
    }
}
