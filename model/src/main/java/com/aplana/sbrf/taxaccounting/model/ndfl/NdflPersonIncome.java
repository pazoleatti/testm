package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Andrey Drunk
 */
public class NdflPersonIncome extends NdflPersonDetail {

    // Код дохода
    private String incomeCode;
    private String incomeType;
    private String oktmo;
    private String kpp;
    // Дата начисления дохода
    private Date incomeAccruedDate;
    private Date incomePayoutDate;
    // Сумма начисленного дохода
    private BigDecimal incomeAccruedSumm;
    // Сумма выплаченного дохода
    private BigDecimal incomePayoutSumm;
    // Общая сумма вычетов
    private BigDecimal totalDeductionsSumm;
    private BigDecimal taxBase;
    // Ставка
    private Integer taxRate;
    // Дата налога
    private Date taxDate;
    // Сумма налога исчисленная
    private Long calculatedTax;
    // Сумма налога удержанная
    private Long withholdingTax;
    private Long notHoldingTax;
    private Long overholdingTax;
    private Long refoundTax;
    // Срок перечисления налога
    private Date taxTransferDate;
    private Date paymentDate;
    private String paymentNumber;
    private Integer taxSumm;

    public static final String TABLE_NAME = "ndfl_person_income";
    public static final String SEQ = "seq_ndfl_person_income";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "operation_id", "oktmo", "kpp", "income_code", "income_type",
            "income_accrued_date", "income_payout_date", "income_accrued_summ", "income_payout_summ", "total_deductions_summ",
            "tax_base", "tax_rate", "tax_date", "calculated_tax", "withholding_tax", "not_holding_tax", "overholding_tax",
            "refound_tax", "tax_transfer_date", "payment_date", "payment_number", "tax_summ"};

    public static final String[] FIELDS = {"id", "ndflPersonId", "rowNum", "operationId", "oktmo", "kpp", "incomeCode", "incomeType",
            "incomeAccruedDate", "incomePayoutDate", "incomeAccruedSumm", "incomePayoutSumm", "totalDeductionsSumm",
            "taxBase", "taxRate", "taxDate", "calculatedTax", "withholdingTax", "notHoldingTax", "overholdingTax",
            "refoundTax", "taxTransferDate", "paymentDate", "paymentNumber", "taxSumm"};

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getSeq() {
        return SEQ;
    }

    @Override
    public String[] getColumns() {
        return COLUMNS;
    }

    @Override
    public String[] getFields() {
        return FIELDS;
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


    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
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

    public Long getCalculatedTax() {
        return calculatedTax;
    }

    public void setCalculatedTax(Long calculatedTax) {
        this.calculatedTax = calculatedTax;
    }

    public Long getWithholdingTax() {
        return withholdingTax;
    }

    public void setWithholdingTax(Long withholdingTax) {
        this.withholdingTax = withholdingTax;
    }

    public Long getNotHoldingTax() {
        return notHoldingTax;
    }

    public void setNotHoldingTax(Long notHoldingTax) {
        this.notHoldingTax = notHoldingTax;
    }

    public Long getOverholdingTax() {
        return overholdingTax;
    }

    public void setOverholdingTax(Long overholdingTax) {
        this.overholdingTax = overholdingTax;
    }

    public Long getRefoundTax() {
        return refoundTax;
    }

    public void setRefoundTax(Long refoundTax) {
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
