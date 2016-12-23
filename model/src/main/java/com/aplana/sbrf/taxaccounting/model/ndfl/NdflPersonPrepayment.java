package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Andrey Drunk
 */
public class NdflPersonPrepayment extends NdflPersonDetail {

    private BigDecimal summ;
    private String notifNum;
    private Date notifDate;
    private String notifSource;

    public static final String TABLE_NAME = "ndfl_person_prepayment";
    public static final String SEQ = "seq_ndfl_person_prepayment";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "summ", "notif_num", "notif_date", "notif_source"};

    public Object[] createPreparedStatementArgs() {
        return new Object[]{id, ndflPersonId, rowNum, summ, notifNum, notifDate, notifSource};
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
}
