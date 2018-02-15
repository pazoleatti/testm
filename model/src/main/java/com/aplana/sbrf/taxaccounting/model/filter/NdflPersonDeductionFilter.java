package com.aplana.sbrf.taxaccounting.model.filter;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки "Сведения о вычетах" страницу РНУ НДФЛ
 */
public class NdflPersonDeductionFilter {
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
     * Код вычета
     */
    private String deductionCode;
    /**
     * Код дохода
     */
    private String incomeCode;
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

    public String getDeductionCode() {
        return deductionCode;
    }

    public void setDeductionCode(String deductionCode) {
        this.deductionCode = deductionCode;
    }

    public String getIncomeCode() {
        return incomeCode;
    }

    public void setIncomeCode(String incomeCode) {
        this.incomeCode = incomeCode;
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
