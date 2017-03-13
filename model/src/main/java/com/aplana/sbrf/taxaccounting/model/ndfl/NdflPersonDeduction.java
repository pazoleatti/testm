package com.aplana.sbrf.taxaccounting.model.ndfl;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Стандартные, социальные и имущественные налоговые вычеты (Раздел 3)
 * @author Andrey Drunk
 */
public class NdflPersonDeduction extends NdflPersonOperation {

    // Код вычета (Графа 3)
    private String typeCode;
    // Тип вычета (Графа 4)
    private String notifType;
    // Дата выдачи уведомления
    private Date notifDate;
    // Номер (Графа 6)
    private String notifNum;
    // Код источника (Графа 7)
    private String notifSource;
    // Сумма вычета согласно документу (Графа 8)
    private BigDecimal notifSumm;
    // Дата начисления дохода (Графа 10)
    private Date incomeAccrued;
    // Код дохода (Графа 11)
    private String incomeCode;
    // Сумма начисленного дохода (Графа 12)
    private BigDecimal incomeSumm;
    // Дата применения вычета в предыдущем периоде
    private Date periodPrevDate;
    private BigDecimal periodPrevSumm;
    // Дата применения вычета в текущем периоде (Графа 15)
    private Date periodCurrDate;
    // Сумма применения вычета в текущем периоде (Графа 16)
    private BigDecimal periodCurrSumm;

    public static final String TABLE_NAME = "ndfl_person_deduction";
    public static final String SEQ = "seq_ndfl_person_deduction";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "operation_id", "type_code",
            "notif_type", "notif_date", "notif_num", "notif_source",
            "notif_summ", "income_accrued", "income_code", "income_summ",
            "period_prev_date", "period_prev_summ",
            "period_curr_date", "period_curr_summ", "source_id"};

    public static final String[] FIELDS = {"id", "ndflPersonId", "rowNum", "operationId", "typeCode",
            "notifType", "notifDate", "notifNum", "notifSource",
            "notifSumm", "incomeAccrued", "incomeCode", "incomeSumm",
            "periodPrevDate", "periodPrevSumm",
            "periodCurrDate", "periodCurrSumm", "sourceId"};

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

    @Override
    public String toString() {
        return "NdflPersonDeduction{" +
                "typeCode='" + typeCode + '\'' +
                ", notifType='" + notifType + '\'' +
                ", notifDate=" + notifDate +
                ", notifNum='" + notifNum + '\'' +
                ", notifSource='" + notifSource + '\'' +
                ", notifSumm=" + notifSumm +
                ", incomeAccrued=" + incomeAccrued +
                ", incomeCode='" + incomeCode + '\'' +
                ", incomeSumm=" + incomeSumm +
                ", periodPrevDate=" + periodPrevDate +
                ", periodPrevSumm=" + periodPrevSumm +
                ", periodCurrDate=" + periodCurrDate +
                ", periodCurrSumm=" + periodCurrSumm +
                '}';
    }
}
