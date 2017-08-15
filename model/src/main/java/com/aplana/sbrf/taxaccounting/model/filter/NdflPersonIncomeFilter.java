package com.aplana.sbrf.taxaccounting.model.filter;

import org.joda.time.LocalDateTime;

/**
 * Модель для параметров Фильтра вкладки "Сведения о доходах и НДФЛ" страницу РНУ НДФЛ
 */
public class NdflPersonIncomeFilter {
    /**
     * id формы
     */
    private long declarationDataId;
    /**
     * ИНП
     */
    private String inp;
    /**
     * id операции
     */
    private String operationId;
    /**
     * КПП
     */
    private String kpp;
    /**
     * ОКТМО
     */
    private String oktmo;
    /**
     * Код дохода
     */
    private String incomeCode;
    /**
     * Признак дохода
     */
    private String incomeAttr;
    /**
     * Процентная ставка
     */
    private String taxRate;
    /**
     * Номер платежного поручения
     */
    private String numberPaymentOrder;
    /**
     * Срок перечисления в бюджет с
     */
    private LocalDateTime transferDateFrom;
    /**
     * Срок перечисления в бюджет по
     */
    private LocalDateTime transferDateTo;
    /**
     * Дата расчета НДФЛ с
     */
    private LocalDateTime calculationDateFrom;
    /**
     * Дата расчета НДФЛ по
     */
    private LocalDateTime calculationDateTo;
    /**
     * Дата платежного поручения с
     */
    private LocalDateTime paymentDateFrom;
    /**
     * Дата платежного поручения по
     */
    private LocalDateTime paymentDateTo;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getOktmo() {
        return oktmo;
    }

    public void setOktmo(String oktmo) {
        this.oktmo = oktmo;
    }

    public String getIncomeCode() {
        return incomeCode;
    }

    public void setIncomeCode(String incomeCode) {
        this.incomeCode = incomeCode;
    }

    public String getIncomeAttr() {
        return incomeAttr;
    }

    public void setIncomeAttr(String incomeAttr) {
        this.incomeAttr = incomeAttr;
    }

    public String getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public String getNumberPaymentOrder() {
        return numberPaymentOrder;
    }

    public void setNumberPaymentOrder(String numberPaymentOrder) {
        this.numberPaymentOrder = numberPaymentOrder;
    }

    public LocalDateTime getTransferDateFrom() {
        return transferDateFrom;
    }

    public void setTransferDateFrom(LocalDateTime transferDateFrom) {
        this.transferDateFrom = transferDateFrom;
    }

    public LocalDateTime getTransferDateTo() {
        return transferDateTo;
    }

    public void setTransferDateTo(LocalDateTime transferDateTo) {
        this.transferDateTo = transferDateTo;
    }

    public LocalDateTime getCalculationDateFrom() {
        return calculationDateFrom;
    }

    public void setCalculationDateFrom(LocalDateTime calculationDateFrom) {
        this.calculationDateFrom = calculationDateFrom;
    }

    public LocalDateTime getCalculationDateTo() {
        return calculationDateTo;
    }

    public void setCalculationDateTo(LocalDateTime calculationDateTo) {
        this.calculationDateTo = calculationDateTo;
    }

    public LocalDateTime getPaymentDateFrom() {
        return paymentDateFrom;
    }

    public void setPaymentDateFrom(LocalDateTime paymentDateFrom) {
        this.paymentDateFrom = paymentDateFrom;
    }

    public LocalDateTime getPaymentDateTo() {
        return paymentDateTo;
    }

    public void setPaymentDateTo(LocalDateTime paymentDateTo) {
        this.paymentDateTo = paymentDateTo;
    }
}
