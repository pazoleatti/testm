package com.aplana.sbrf.taxaccounting.model.ndfl;

import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Andrey Drunk
 * Cведения о доходах в виде авансовых платежей
 */
public class NdflPersonPrepayment extends NdflPersonOperation {

    //ИНП (Графа 2)
    private String inp;

    // Сумма фиксированного авансового платежа (Графа 4)
    private BigDecimal summ;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Номер уведомления
    private String notifNum;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Дата выдачи уведомления
    private LocalDateTime notifDate;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)
    private String notifSource;

    public static final String TABLE_NAME = "ndfl_person_prepayment";
    public static final String SEQ = "seq_ndfl_person_prepayment";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "operation_id", "summ", "notif_num", "notif_date", "notif_source", "source_id"};
    public static final String[] FIELDS = {"id", "ndflPersonId", "rowNum", "operationId", "summ", "notifNum", "notifDate", "notifSource", "sourceId"};

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getSeq() {
        return SEQ;
    }

    @Override
    public String[] getColumns() {
        return COLUMNS;
    }

    @Override
    public String[] getFields() {
        return FIELDS;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
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

    public LocalDateTime getNotifDate() {
        return notifDate;
    }

    public void setNotifDate(LocalDateTime notifDate) {
        this.notifDate = notifDate;
    }

    public String getNotifSource() {
        return notifSource;
    }

    public void setNotifSource(String notifSource) {
        this.notifSource = notifSource;
    }

    @Override
    public String toString() {
        return "NdflPersonPrepayment{" +
                "summ=" + summ +
                ", inp='" + inp + '\'' +
                ", notifNum='" + notifNum + '\'' +
                ", notifDate=" + notifDate +
                ", notifSource='" + notifSource + '\'' +
                '}';
    }
}
