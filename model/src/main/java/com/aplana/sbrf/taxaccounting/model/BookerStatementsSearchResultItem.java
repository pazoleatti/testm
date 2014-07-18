package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * @author lhaziev
 */
public class BookerStatementsSearchResultItem implements Serializable {
    private static final long serialVersionUID = -48100343545156L;

    // Идентификатор отчётного периода
    private Integer accountPeriodId;
    // Название отчётного периода
    private String accountPeriodName;
    // Год отчётного периода
    private Integer accountPeriodYear;
    // Идентификатор подразделения
    private Integer departmentId;
    // Идентификатор вида налоговой формы
    private Integer bookerStatementsTypeId;


    public Integer getAccountPeriodId() {
        return accountPeriodId;
    }

    public void setAccountPeriodId(Integer accountPeriodId) {
        this.accountPeriodId = accountPeriodId;
    }

    public String getAccountPeriodName() {
        return accountPeriodName;
    }

    public void setAccountPeriodName(String accountPeriodName) {
        this.accountPeriodName = accountPeriodName;
    }

    public Integer getAccountPeriodYear() {
        return accountPeriodYear;
    }

    public void setAccountPeriodYear(Integer accountPeriodYear) {
        this.accountPeriodYear = accountPeriodYear;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getBookerStatementsTypeId() {
        return bookerStatementsTypeId;
    }

    public void setBookerStatementsTypeId(Integer bookerStatementsTypeId) {
        this.bookerStatementsTypeId = bookerStatementsTypeId;
    }

}
