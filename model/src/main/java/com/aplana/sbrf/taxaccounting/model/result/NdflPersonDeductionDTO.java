package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.json.ISODateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.Date;

public class NdflPersonDeductionDTO {

    /**
     * Ссылка на физлицо
     */
    private Long ndflPersonId;

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
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date notifDate;

    // Документ о праве на налоговый вычет.Номер (Графа 6)
    private String notifNum;

    // Документ о праве на налоговый вычет.Код источника (Графа 7)
    private String notifSource;

    // Документ о праве на налоговый вычет.Сумма (Графа 8)
    private BigDecimal notifSumm;

    // Начисленный доход.Дата (Графа 10)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date incomeAccrued;

    // Начисленный доход.Код дохода (Графа 11)
    private String incomeCode;

    // Начисленный доход.Сумма (Графа 12)
    private BigDecimal incomeSumm;

    // Применение вычета.Предыдущий период.Дата (Графа 13)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date periodPrevDate;

    // Применение вычета.Предыдущий период.Сумма (Графа 14)
    private BigDecimal periodPrevSumm;

    // Применение вычета.Текущий период.Дата (Графа 15)
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date periodCurrDate;

    // Применение вычета.Текущий период.Сумма (Графа 16)
    private BigDecimal periodCurrSumm;

    private String inp;

    private Long id;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    private String modifiedBy;

    public NdflPersonDeductionDTO() {
    }

    public NdflPersonDeductionDTO(String operationId, Long sourceId, BigDecimal rowNum, String typeCode, String notifType, Date notifDate, String notifNum, String notifSource, BigDecimal notifSumm, Date incomeAccrued, String incomeCode, BigDecimal incomeSumm, Date periodPrevDate, BigDecimal periodPrevSumm, Date periodCurrDate, BigDecimal periodCurrSumm, String inp) {
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

    public Date getNotifDate() {
        return notifDate;
    }

    public void setNotifDate(Date notifDate) {
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

    public Date getIncomeAccrued() {
        return incomeAccrued;
    }

    public void setIncomeAccrued(Date incomeAccrued) {
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

    public Date getPeriodPrevDate() {
        return periodPrevDate;
    }

    public void setPeriodPrevDate(Date periodPrevDate) {
        this.periodPrevDate = periodPrevDate;
    }

    public BigDecimal getPeriodPrevSumm() {
        return periodPrevSumm;
    }

    public void setPeriodPrevSumm(BigDecimal periodPrevSumm) {
        this.periodPrevSumm = periodPrevSumm;
    }

    public Date getPeriodCurrDate() {
        return periodCurrDate;
    }

    public void setPeriodCurrDate(Date periodCurrDate) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Long getNdflPersonId() {
        return ndflPersonId;
    }

    public void setNdflPersonId(Long ndflPersonId) {
        this.ndflPersonId = ndflPersonId;
    }
}
