package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Сведения о доходах физического лица
 * @author Andrey Drunk
 */
public class NdflPersonIncome extends NdflPersonOperation {

    // Доход.Вид.Код (Графа 4) (КодДох)
    private String incomeCode;

    // Доход.Вид.Признак (Графа 5)
    private String incomeType;

    // Доход.Дата.Начисление (Графа 6)
    private Date incomeAccruedDate;

    // Доход.Дата.Выплата (Графа 7)
    private Date incomePayoutDate;

    // Доход.Источник выплаты.ОКТМО (Графа 8)
    private String oktmo;

    // Доход.Источник выплаты.КПП (Графа 9)
    private String kpp;

    // Доход.Сумма.Начисление (Графа 10)
    private BigDecimal incomeAccruedSumm;

    // Доход.Сумма.Выплата (Графа 11)
    private BigDecimal incomePayoutSumm;

    // Сумма вычета (Графа 12)
    private BigDecimal totalDeductionsSumm;

    // Налоговая база (Графа 13)
    private BigDecimal taxBase;

    // НДФЛ.Процентная ставка (Графа 14)
    private Integer taxRate;

    // НДФЛ.Расчет.Дата (Графа 15)
    private Date taxDate;

    // НДФЛ.Расчет.Сумма.Исчисленный (Графа 16)
    private BigDecimal calculatedTax;

    // НДФЛ.Расчет.Сумма.Удержанный (Графа 17) (НУ)
    private BigDecimal withholdingTax;

    // НДФЛ.Расчет.Сумма.Не удержанный (Графа 18)
    private BigDecimal notHoldingTax;

    // НДФЛ.Расчет.Сумма.Излишне удержанный (Графа 19)
    private BigDecimal overholdingTax;

    // НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику (Графа 20)
    private Long refoundTax;

    // НДФЛ.Перечисление в бюджет.Срок (Графа 21)
    private Date taxTransferDate;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Дата (Графа 22)
    private Date paymentDate;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Номер (Графа 23)
    private String paymentNumber;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)
    private Long taxSumm;

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

    public BigDecimal getCalculatedTax() {
        return calculatedTax;
    }

    public void setCalculatedTax(BigDecimal calculatedTax) {
        this.calculatedTax = calculatedTax;
    }

    public BigDecimal getWithholdingTax() {
        return withholdingTax;
    }

    public void setWithholdingTax(BigDecimal withholdingTax) {
        this.withholdingTax = withholdingTax;
    }

    public BigDecimal getNotHoldingTax() {
        return notHoldingTax;
    }

    public void setNotHoldingTax(BigDecimal notHoldingTax) {
        this.notHoldingTax = notHoldingTax;
    }

    public BigDecimal getOverholdingTax() {
        return overholdingTax;
    }

    public void setOverholdingTax(BigDecimal overholdingTax) {
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

    public Long getTaxSumm() {
        return taxSumm;
    }

    public void setTaxSumm(Long taxSumm) {
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
