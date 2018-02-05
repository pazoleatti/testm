package com.aplana.sbrf.taxaccounting.model.result;

import java.math.BigDecimal;
import java.util.Date;

public class NdflPersonPrepaymentDTO {

    /**
     * Идентификатор операции
     */
    protected String operationId;

    protected Long sourceId;

    /**
     * Порядковый номер строки
     */
    protected BigDecimal rowNum;

    // Сумма фиксированного авансового платежа (Графа 4)
    private BigDecimal summ;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Номер уведомления
    private String notifNum;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Дата выдачи уведомления
    private Date notifDate;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)
    private String notifSource;

    private String inp;

    public NdflPersonPrepaymentDTO() {
    }

    public NdflPersonPrepaymentDTO(String operationId, Long sourceId, BigDecimal rowNum, BigDecimal summ, String notifNum, Date notifDate, String notifSource, String inp) {
        this.operationId = operationId;
        this.sourceId = sourceId;
        this.rowNum = rowNum;
        this.summ = summ;
        this.notifNum = notifNum;
        this.notifDate = notifDate;
        this.notifSource = notifSource;
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

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }

    public String getNotifNum() {
        return notifNum;
    }

    public void setNotifNum(String notifNum) {
        this.notifNum = notifNum;
    }

    public Date getNotifDate() {
        return notifDate;
    }

    public void setNotifDate(Date notifDate) {
        this.notifDate = notifDate;
    }

    public String getNotifSource() {
        return notifSource;
    }

    public void setNotifSource(String notifSource) {
        this.notifSource = notifSource;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }
}
