package com.aplana.sbrf.taxaccounting.model.filter;

import org.joda.time.LocalDateTime;

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
    private LocalDateTime notifDateFrom;
    /**
     * Дата выдачи уведомления по
     */
    private LocalDateTime notifDateTo;

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

    public LocalDateTime getNotifDateFrom() {
        return notifDateFrom;
    }

    public void setNotifDateFrom(LocalDateTime notifDateFrom) {
        this.notifDateFrom = notifDateFrom;
    }

    public LocalDateTime getNotifDateTo() {
        return notifDateTo;
    }

    public void setNotifDateTo(LocalDateTime notifDateTo) {
        this.notifDateTo = notifDateTo;
    }
}
