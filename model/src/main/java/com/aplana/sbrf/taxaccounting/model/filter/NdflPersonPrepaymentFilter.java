package com.aplana.sbrf.taxaccounting.model.filter;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

import java.util.Date;
import java.util.List;

/**
 * Модель для параметров Фильтра вкладки "Сведения о доходах в виде авансовых платежей" страницу РНУ НДФЛ
 */
public class NdflPersonPrepaymentFilter {
    /**
     * id операции
     */
    private String operationId;
    /**
     * Номер уведомления
     */
    private String notifNum;
    /**
     * Код НО, выдавшего уведомления
     */
    private String notifSource;
    /**
     * Дата выдачи уведомления с
     */
    private Date notifDateFrom;
    /**
     * Дата выдачи уведомления по
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

    /**
     * АСНУ
     */
    private List<RefBookAsnu> asnu;

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
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

    public List<RefBookAsnu> getAsnu() {
        return asnu;
    }

    public void setAsnu(List<RefBookAsnu> asnu) {
        this.asnu = asnu;
    }
}
