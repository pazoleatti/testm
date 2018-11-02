package com.aplana.sbrf.taxaccounting.model.filter;

import com.aplana.sbrf.taxaccounting.model.URM;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Модель для параметров Фильтра вкладки "Сведения о доходах и НДФЛ" страницу РНУ НДФЛ
 */
public class NdflPersonIncomeFilter {
    /**
     * Общий фильтр
     */
    private NdflFilter ndflFilter;
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
     * Процентная ставка
     */
    private String taxRate;
    /**
     * Код дохода
     */
    private String incomeCode;
    /**
     * Признак дохода
     */
    private String incomeAttr;

    /**
     * Дата начисления с
     */
    private Date accruedDateFrom;
    /**
     * Дата начисления по
     */
    private Date accruedDateTo;
    /**
     * Дата выплаты с
     */
    private Date payoutDateFrom;
    /**
     * Дата выплаты по
     */
    private Date payoutDateTo;
    /**
     * Дата расчета НДФЛ с
     */
    private Date calculationDateFrom;
    /**
     * Дата расчета НДФЛ по
     */
    private Date calculationDateTo;
    /**
     * Срок перечисления в бюджет с
     */
    private Date transferDateFrom;
    /**
     * Срок перечисления в бюджет по
     */
    private Date transferDateTo;
    /**
     * Дата платежного поручения с
     */
    private Date paymentDateFrom;
    /**
     * Дата платежного поручения по
     */
    private Date paymentDateTo;
    /**
     * Номер платежного поручения
     */
    private String numberPaymentOrder;

    /**
     * Данные УРМ
     */
    private List<URM> urmList = new ArrayList<>();;

    /**
     * Номер строки
     */
    private String rowNum;
    /**
     * Идентификатор строки
     */
    private String id;
    /**
     * Дата редактирования с
     */
    private Date modifiedDateFrom;
    /**
     * Дата редактирования по
     */
    private Date modifiedDateTo;
    /**
     * Обновил
     */
    private String modifiedBy;

    /**
     * АСНУ
     */
    private List<RefBookAsnu> asnu;

    public NdflFilter getNdflFilter() {
        return ndflFilter;
    }

    public void setNdflFilter(NdflFilter ndflFilter) {
        this.ndflFilter = ndflFilter;
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

    public Date getAccruedDateFrom() {
        return accruedDateFrom;
    }

    public void setAccruedDateFrom(Date accruedDateFrom) {
        this.accruedDateFrom = accruedDateFrom;
    }

    public Date getAccruedDateTo() {
        return accruedDateTo;
    }

    public void setAccruedDateTo(Date accruedDateTo) {
        this.accruedDateTo = accruedDateTo;
    }

    public Date getPayoutDateFrom() {
        return payoutDateFrom;
    }

    public void setPayoutDateFrom(Date payoutDateFrom) {
        this.payoutDateFrom = payoutDateFrom;
    }

    public Date getPayoutDateTo() {
        return payoutDateTo;
    }

    public void setPayoutDateTo(Date payoutDateTo) {
        this.payoutDateTo = payoutDateTo;
    }

    public List<URM> getUrmList() {
        return urmList;
    }

    public void setUrmList(List<URM> urmList) {
        this.urmList = urmList;
    }

    public String getRowNum() {
        return rowNum;
    }

    public void setRowNum(String rowNum) {
        this.rowNum = rowNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getModifiedDateFrom() {
        return modifiedDateFrom;
    }

    public void setModifiedDateFrom(Date modifiedDateFrom) {
        this.modifiedDateFrom = modifiedDateFrom;
    }

    public Date getModifiedDateTo() {
        return modifiedDateTo;
    }

    public void setModifiedDateTo(Date modifiedDateTo) {
        this.modifiedDateTo = modifiedDateTo;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public List<RefBookAsnu> getAsnu() {
        return asnu;
    }

    public void setAsnu(List<RefBookAsnu> asnu) {
        this.asnu = asnu;
    }
}
