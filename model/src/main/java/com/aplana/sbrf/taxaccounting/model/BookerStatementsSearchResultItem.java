package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * @author lhaziev
 */
public class BookerStatementsSearchResultItem implements Serializable {
    private static final long serialVersionUID = -48100343545156L;

    // Идентификатор отчётного периода
    private Integer reportPeriodId;
    // Название отчётного периода
    private String reportPeriodName;
    // Год отчётного периода
    private Integer reportPeriodYear;
    // Идентификатор подразделения
    private Integer departmentId;
    // Идентификатор вида налоговой формы
    private Integer BookerStatementsTypeId;


    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public String getReportPeriodName() {
        return reportPeriodName;
    }

    public void setReportPeriodName(String reportPeriodName) {
        this.reportPeriodName = reportPeriodName;
    }

    public Integer getReportPeriodYear() {
        return reportPeriodYear;
    }

    public void setReportPeriodYear(Integer reportPeriodYear) {
        this.reportPeriodYear = reportPeriodYear;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getBookerStatementsTypeId() {
        return BookerStatementsTypeId;
    }

    public void setBookerStatementsTypeId(Integer bookerStatementsTypeId) {
        BookerStatementsTypeId = bookerStatementsTypeId;
    }

}
