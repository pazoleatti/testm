package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Сведения о доходах физического лица
 * @author Andrey Drunk
 */
public class NdflPersonIncome extends NdflPersonOperation {

    // Код вида дохода (Графа 4)
    private String incomeCode;
    // Признак вида дохода (Графа 5)
    private String incomeType;
    private String oktmo;
    private String kpp;
    // Дата начисления дохода (Графа 6)
    private Date incomeAccruedDate;
    // Дата выплаты дохода (Графа 7)
    private Date incomePayoutDate;
    // Сумма начисленного дохода (Графа 10)
    private BigDecimal incomeAccruedSumm;
    // Сумма выплаченного дохода
    private BigDecimal incomePayoutSumm;
    // Общая сумма вычетов (Графа 12)
    private BigDecimal totalDeductionsSumm;
    // Налоговая база (Графа 13)
    private BigDecimal taxBase;
    // Ставка (Графа 14)
    private Integer taxRate;

    /**
     * Дата налога (Графа 15)
     * Если заполнено поле "Сумма налога исчисленная", то это "Дата исчисления"
     * Если заполнено поле "Сумма налога удержанная", то это "Дата удержания"
     */
    private Date taxDate;
    // Сумма налога исчисленная (Графа 16)
    private Long calculatedTax;
    // Сумма налога удержанная (Графа 17)
    private Long withholdingTax;
    // Сумма налога, не удержанная налоговым агентом (Графа 18)
    private Long notHoldingTax;
    // Сумма налога, излишне удержанная налоговым агентом (Графа 19)
    private Long overholdingTax;
    // Сумма возвращенного налога (Графа 20)
    private Long refoundTax;
    // Срок (дата) перечисления налога
    private Date taxTransferDate;
    // Дата платежного поручения
    private Date paymentDate;
    // Номер платежного поручения перечисления налога в бюджет
    private String paymentNumber;
    // Сумма налога перечисленная (Графа 24)
    private Integer taxSumm;

    public static final String TABLE_NAME = "ndfl_person_income";
    public static final String SEQ = "seq_ndfl_person_income";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "operation_id", "oktmo", "kpp", "income_code", "income_type",
            "income_accrued_date", "income_payout_date", "income_accrued_summ", "income_payout_summ", "total_deductions_summ",
            "tax_base", "tax_rate", "tax_date", "calculated_tax", "withholding_tax", "not_holding_tax", "overholding_tax",
            "refound_tax", "tax_transfer_date", "payment_date", "payment_number", "tax_summ", "source_id"};

    public static final String[] FIELDS = {"id", "ndflPersonId", "rowNum", "operationId", "oktmo", "kpp", "incomeCode", "incomeType",
            "incomeAccruedDate", "incomePayoutDate", "incomeAccruedSumm", "incomePayoutSumm", "totalDeductionsSumm",
            "taxBase", "taxRate", "taxDate", "calculatedTax", "withholdingTax", "notHoldingTax", "overholdingTax",
            "refoundTax", "taxTransferDate", "paymentDate", "paymentNumber", "taxSumm", "sourceId"};

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

    @Override
    public String toString() {
        return "NdflPersonIncome{" +
                "incomeCode='" + incomeCode + '\'' +
                ", incomeType='" + incomeType + '\'' +
                ", oktmo='" + oktmo + '\'' +
                ", kpp='" + kpp + '\'' +
                ", incomeAccruedDate=" + incomeAccruedDate +
                ", incomePayoutDate=" + incomePayoutDate +
                ", incomeAccruedSumm=" + incomeAccruedSumm +
                ", incomePayoutSumm=" + incomePayoutSumm +
                ", totalDeductionsSumm=" + totalDeductionsSumm +
                ", taxBase=" + taxBase +
                ", taxRate=" + taxRate +
                ", taxDate=" + taxDate +
                ", calculatedTax=" + calculatedTax +
                ", withholdingTax=" + withholdingTax +
                ", notHoldingTax=" + notHoldingTax +
                ", overholdingTax=" + overholdingTax +
                ", refoundTax=" + refoundTax +
                ", taxTransferDate=" + taxTransferDate +
                ", paymentDate=" + paymentDate +
                ", paymentNumber='" + paymentNumber + '\'' +
                ", taxSumm=" + taxSumm +
                '}';
    }
}
