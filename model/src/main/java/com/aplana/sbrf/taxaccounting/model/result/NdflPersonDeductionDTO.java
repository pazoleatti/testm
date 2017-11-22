package com.aplana.sbrf.taxaccounting.model.result;

import org.joda.time.LocalDateTime;

import java.math.BigDecimal;

public class NdflPersonDeductionDTO {

    /**
     * Идентификатор операции
     */
    protected String operationId;

    protected Long sourceId;

    /**
     * Порядковый номер строки
     */
    protected BigDecimal rowNum;

    // Код вычета (Графа 3)
    private String typeCode;

    // Документ о праве на налоговый вычет.Тип (Графа 4)
    private String notifType;

    // Документ о праве на налоговый вычет.Дата (Графа 5)
    private LocalDateTime notifDate;

    // Документ о праве на налоговый вычет.Номер (Графа 6)
    private String notifNum;

    // Документ о праве на налоговый вычет.Код источника (Графа 7)
    private String notifSource;

    // Документ о праве на налоговый вычет.Сумма (Графа 8)
    private BigDecimal notifSumm;

    // Начисленный доход.Дата (Графа 10)
    private LocalDateTime incomeAccrued;

    // Начисленный доход.Код дохода (Графа 11)
    private String incomeCode;

    // Начисленный доход.Сумма (Графа 12)
    private BigDecimal incomeSumm;

    // Применение вычета.Предыдущий период.Дата (Графа 13)
    private LocalDateTime periodPrevDate;

    // Применение вычета.Предыдущий период.Сумма (Графа 14)
    private BigDecimal periodPrevSumm;

    // Применение вычета.Текущий период.Дата (Графа 15)
    private LocalDateTime periodCurrDate;

    // Применение вычета.Текущий период.Сумма (Графа 16)
    private BigDecimal periodCurrSumm;

    private String inp;

    public NdflPersonDeductionDTO(String operationId, Long sourceId, BigDecimal rowNum, String typeCode, String notifType, LocalDateTime notifDate, String notifNum, String notifSource, BigDecimal notifSumm, LocalDateTime incomeAccrued, String incomeCode, BigDecimal incomeSumm, LocalDateTime periodPrevDate, BigDecimal periodPrevSumm, LocalDateTime periodCurrDate, BigDecimal periodCurrSumm, String inp) {
        this.operationId = operationId;
        this.sourceId = sourceId;
        this.rowNum = rowNum;
        this.typeCode = typeCode;
        this.notifType = notifType;
        this.notifDate = notifDate;
        this.notifNum = notifNum;
        this.notifSource = notifSource;
        this.notifSumm = notifSumm;
        this.incomeAccrued = incomeAccrued;
        this.incomeCode = incomeCode;
        this.incomeSumm = incomeSumm;
        this.periodPrevDate = periodPrevDate;
        this.periodPrevSumm = periodPrevSumm;
        this.periodCurrDate = periodCurrDate;
        this.periodCurrSumm = periodCurrSumm;
        this.inp = inp;
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

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getNotifType() {
        return notifType;
    }

    public void setNotifType(String notifType) {
        this.notifType = notifType;
    }

    public LocalDateTime getNotifDate() {
        return notifDate;
    }

    public void setNotifDate(LocalDateTime notifDate) {
        this.notifDate = notifDate;
    }

    public String getNotifNum() {
        return notifNum;
    }

    public void setNotifNum(String notifNum) {
        this.notifNum = notifNum;
    }

    public String getNotifSource() {
        return notifSource;
    }

    public void setNotifSource(String notifSource) {
        this.notifSource = notifSource;
    }

    public BigDecimal getNotifSumm() {
        return notifSumm;
    }

    public void setNotifSumm(BigDecimal notifSumm) {
        this.notifSumm = notifSumm;
    }

    public LocalDateTime getIncomeAccrued() {
        return incomeAccrued;
    }

    public void setIncomeAccrued(LocalDateTime incomeAccrued) {
        this.incomeAccrued = incomeAccrued;
    }

    public String getIncomeCode() {
        return incomeCode;
    }

    public void setIncomeCode(String incomeCode) {
        this.incomeCode = incomeCode;
    }

    public BigDecimal getIncomeSumm() {
        return incomeSumm;
    }

    public void setIncomeSumm(BigDecimal incomeSumm) {
        this.incomeSumm = incomeSumm;
    }

    public LocalDateTime getPeriodPrevDate() {
        return periodPrevDate;
    }

    public void setPeriodPrevDate(LocalDateTime periodPrevDate) {
        this.periodPrevDate = periodPrevDate;
    }

    public BigDecimal getPeriodPrevSumm() {
        return periodPrevSumm;
    }

    public void setPeriodPrevSumm(BigDecimal periodPrevSumm) {
        this.periodPrevSumm = periodPrevSumm;
    }

    public LocalDateTime getPeriodCurrDate() {
        return periodCurrDate;
    }

    public void setPeriodCurrDate(LocalDateTime periodCurrDate) {
        this.periodCurrDate = periodCurrDate;
    }

    public BigDecimal getPeriodCurrSumm() {
        return periodCurrSumm;
    }

    public void setPeriodCurrSumm(BigDecimal periodCurrSumm) {
        this.periodCurrSumm = periodCurrSumm;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }
}