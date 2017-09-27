package com.aplana.sbrf.taxaccounting.model.filter;

import java.util.Date;

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
    private Date transferDateFrom;
    /**
     * Срок перечисления в бюджет по
     */
    private Date transferDateTo;
    /**
     * Дата расчета НДФЛ с
     */
    private Date calculationDateFrom;
    /**
     * Дата расчета НДФЛ по
     */
    private Date calculationDateTo;
    /**
     * Дата платежного поручения с
     */
    private Date paymentDateFrom;
    /**
     * Дата платежного поручения по
     */
    private Date paymentDateTo;

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

    public Date getTransferDateFrom() {
        return transferDateFrom;
    }

    public void setTransferDateFrom(Date transferDateFrom) {
        this.transferDateFrom = transferDateFrom;
    }

    public Date getTransferDateTo() {
        return transferDateTo;
    }

    public void setTransferDateTo(Date transferDateTo) {
        this.transferDateTo = transferDateTo;
    }

    public Date getCalculationDateFrom() {
        return calculationDateFrom;
    }

    public void setCalculationDateFrom(Date calculationDateFrom) {
        this.calculationDateFrom = calculationDateFrom;
    }

    public Date getCalculationDateTo() {
        return calculationDateTo;
    }

    public void setCalculationDateTo(Date calculationDateTo) {
        this.calculationDateTo = calculationDateTo;
    }

    public Date getPaymentDateFrom() {
        return paymentDateFrom;
    }

    public void setPaymentDateFrom(Date paymentDateFrom) {
        this.paymentDateFrom = paymentDateFrom;
    }

    public Date getPaymentDateTo() {
        return paymentDateTo;
    }

    public void setPaymentDateTo(Date paymentDateTo) {
        this.paymentDateTo = paymentDateTo;
    }
}
