package com.aplana.sbrf.taxaccounting.model.filter;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки "Сведения о доходах в виде авансовых платежей" страницу РНУ НДФЛ
 */
public class NdflPersonPrepaymentFilter {
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
}
