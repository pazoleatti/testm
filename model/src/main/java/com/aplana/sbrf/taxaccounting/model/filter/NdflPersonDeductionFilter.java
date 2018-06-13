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
     * Дата применения вычета с
     */
    private Date periodPrevDateFrom;
    /**
     * Дата применения вычета по
     */
    private Date periodPrevDateTo;
    /**
     * Дата текущего вычета с
     */
    private Date deductionDateFrom;
    /**
     * Дата текущего вычета по
     */
    private Date deductionDateTo;

    /**
     * Тип подтв. документа
     */
    private String notifType;
    /**
     * Номер подтв. документа
     */
    private String notifNum;
    /**
     * Код источника подтв. документа
     */
    private String notifSource;
    /**
     * Дата подтв. документа с
     */
    private Date notifDateFrom;
    /**
     * Дата подтв. документа по
     */
    private Date notifDateTo;

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

    public Date getPeriodPrevDateFrom() {
        return periodPrevDateFrom;
    }

    public void setPeriodPrevDateFrom(Date periodPrevDateFrom) {
        this.periodPrevDateFrom = periodPrevDateFrom;
    }

    public Date getPeriodPrevDateTo() {
        return periodPrevDateTo;
    }

    public void setPeriodPrevDateTo(Date periodPrevDateTo) {
        this.periodPrevDateTo = periodPrevDateTo;
    }

    public String getNotifType() {
        return notifType;
    }

    public void setNotifType(String notifType) {
        this.notifType = notifType;
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

    public Date getNotifDateFrom() {
        return notifDateFrom;
    }

    public void setNotifDateFrom(Date notifDateFrom) {
        this.notifDateFrom = notifDateFrom;
    }

    public Date getNotifDateTo() {
        return notifDateTo;
    }

    public void setNotifDateTo(Date notifDateTo) {
        this.notifDateTo = notifDateTo;
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
}
