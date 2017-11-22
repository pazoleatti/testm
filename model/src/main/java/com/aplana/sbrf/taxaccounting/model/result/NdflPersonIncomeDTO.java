package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;

public class NdflPersonIncomeDTO {

    // Доход.Вид.Код (Графа 4) (КодДох)
    private String incomeCode;

    // Доход.Вид.Признак (Графа 5)
    private String incomeType;

    // Доход.Дата.Начисление (Графа 6)
    private LocalDateTime incomeAccruedDate;

    // Доход.Дата.Выплата (Графа 7)
    private LocalDateTime incomePayoutDate;

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
    private LocalDateTime taxDate;

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
    private LocalDateTime taxTransferDate;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Дата (Графа 22)
    private LocalDateTime paymentDate;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Номер (Графа 23)
    private String paymentNumber;

    // НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма (Графа 24)
    private Long taxSumm;

    /**
     * Идентификатор операции
     */
    protected String operationId;

    protected Long sourceId;

    /**
     * Порядковый номер строки
     */
    protected BigDecimal rowNum;

    private String inp;

    public NdflPersonIncomeDTO(String incomeCode, String incomeType, LocalDateTime incomeAccruedDate, LocalDateTime incomePayoutDate, String oktmo, String kpp, BigDecimal incomeAccruedSumm, BigDecimal incomePayoutSumm, BigDecimal totalDeductionsSumm, BigDecimal taxBase, Integer taxRate, LocalDateTime taxDate, BigDecimal calculatedTax, BigDecimal withholdingTax, BigDecimal notHoldingTax, BigDecimal overholdingTax, Long refoundTax, LocalDateTime taxTransferDate, LocalDateTime paymentDate, String paymentNumber, Long taxSumm, String operationId, Long sourceId, BigDecimal rowNum, String inp) {
        this.incomeCode = incomeCode;
        this.incomeType = incomeType;
        this.incomeAccruedDate = incomeAccruedDate;
        this.incomePayoutDate = incomePayoutDate;
        this.oktmo = oktmo;
        this.kpp = kpp;
        this.incomeAccruedSumm = incomeAccruedSumm;
        this.incomePayoutSumm = incomePayoutSumm;
        this.totalDeductionsSumm = totalDeductionsSumm;
        this.taxBase = taxBase;
        this.taxRate = taxRate;
        this.taxDate = taxDate;
        this.calculatedTax = calculatedTax;
        this.withholdingTax = withholdingTax;
        this.notHoldingTax = notHoldingTax;
        this.overholdingTax = overholdingTax;
        this.refoundTax = refoundTax;
        this.taxTransferDate = taxTransferDate;
        this.paymentDate = paymentDate;
        this.paymentNumber = paymentNumber;
        this.taxSumm = taxSumm;
        this.operationId = operationId;
        this.sourceId = sourceId;
        this.rowNum = rowNum;
        this.inp = inp;
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

    public LocalDateTime getIncomeAccruedDate() {
        return incomeAccruedDate;
    }

    public void setIncomeAccruedDate(LocalDateTime incomeAccruedDate) {
        this.incomeAccruedDate = incomeAccruedDate;
    }

    public LocalDateTime getIncomePayoutDate() {
        return incomePayoutDate;
    }

    public void setIncomePayoutDate(LocalDateTime incomePayoutDate) {
        this.incomePayoutDate = incomePayoutDate;
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

    public LocalDateTime getTaxDate() {
        return taxDate;
    }

    public void setTaxDate(LocalDateTime taxDate) {
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

    public LocalDateTime getTaxTransferDate() {
        return taxTransferDate;
    }

    public void setTaxTransferDate(LocalDateTime taxTransferDate) {
        this.taxTransferDate = taxTransferDate;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
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

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public BigDecimal getRowNum() {
        return rowNum;
    }

    public void setRowNum(BigDecimal rowNum) {
        this.rowNum = rowNum;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }
}