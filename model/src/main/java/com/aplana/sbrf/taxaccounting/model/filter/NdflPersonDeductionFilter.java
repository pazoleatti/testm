package com.aplana.sbrf.taxaccounting.model.filter;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки "Сведения о вычетах" страницу РНУ НДФЛ
 */
public class NdflPersonDeductionFilter {
    /**
     * id операции
     */
    private String operationId;
    /**
     * Код вычета
     */
    private String deductionCode;
    /**
     * Код дохода
     */
    private String deductionIncomeCode;
    /**
     * Дата начисления дохода с
     */
    private Date incomeAccruedDateFrom;
    /**
     * Дата начисления дохода по
     */
    private Date incomeAccruedDateTo;
    /**
     * Дата текущего вычета с
     */
    private Date deductionDateFrom;
    /**
     * Дата текущего вычета по
     */
    private Date deductionDateTo;

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getDeductionCode() {
        return deductionCode;
    }

    public void setDeductionCode(String deductionCode) {
        this.deductionCode = deductionCode;
    }

    public String getDeductionIncomeCode() {
        return deductionIncomeCode;
    }

    public void setDeductionIncomeCode(String deductionIncomeCode) {
        this.deductionIncomeCode = deductionIncomeCode;
    }

    public Date getIncomeAccruedDateFrom() {
        return incomeAccruedDateFrom;
    }

    public void setIncomeAccruedDateFrom(Date incomeAccruedDateFrom) {
        this.incomeAccruedDateFrom = incomeAccruedDateFrom;
    }

    public Date getIncomeAccruedDateTo() {
        return incomeAccruedDateTo;
    }

    public void setIncomeAccruedDateTo(Date incomeAccruedDateTo) {
        this.incomeAccruedDateTo = incomeAccruedDateTo;
    }

    public Date getDeductionDateFrom() {
        return deductionDateFrom;
    }

    public void setDeductionDateFrom(Date deductionDateFrom) {
        this.deductionDateFrom = deductionDateFrom;
    }

    public Date getDeductionDateTo() {
        return deductionDateTo;
    }

    public void setDeductionDateTo(Date deductionDateTo) {
        this.deductionDateTo = deductionDateTo;
    }
}
