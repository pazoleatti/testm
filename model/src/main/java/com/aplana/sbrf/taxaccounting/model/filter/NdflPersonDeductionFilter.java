package com.aplana.sbrf.taxaccounting.model.filter;

import org.joda.time.LocalDateTime;

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
    private LocalDateTime calculationDateFrom;
    /**
     * Дата начисления дохода по
     */
    private LocalDateTime calculationDateTo;
    /**
     * Дата текущего вычета с
     */
    private LocalDateTime deductionDateFrom;
    /**
     * Дата текущего вычета по
     */
    private LocalDateTime deductionDateTo;

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

    public LocalDateTime getDeductionDateFrom() {
        return deductionDateFrom;
    }

    public void setDeductionDateFrom(LocalDateTime deductionDateFrom) {
        this.deductionDateFrom = deductionDateFrom;
    }

    public LocalDateTime getDeductionDateTo() {
        return deductionDateTo;
    }

    public void setDeductionDateTo(LocalDateTime deductionDateTo) {
        this.deductionDateTo = deductionDateTo;
    }
}
