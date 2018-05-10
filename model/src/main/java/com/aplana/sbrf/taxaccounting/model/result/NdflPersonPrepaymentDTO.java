package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.json.ISODateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.Date;

public class NdflPersonPrepaymentDTO {

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
    protected String rowNum;

    // Сумма фиксированного авансового платежа (Графа 4)
    private BigDecimal summ;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Номер уведомления
    private String notifNum;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Дата выдачи уведомления
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date notifDate;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)
    private String notifSource;

    private String inp;

    private Long id;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    @JsonDeserialize(using = ISODateDeserializer.class)
    private Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    private String modifiedBy;

    public NdflPersonPrepaymentDTO() {
    }

    public NdflPersonPrepaymentDTO(String operationId, Long sourceId, String rowNum, BigDecimal summ, String notifNum, Date notifDate, String notifSource, String inp) {
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

    public String getRowNum() {
        return rowNum;
    }

    public void setRowNum(String rowNum) {
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
