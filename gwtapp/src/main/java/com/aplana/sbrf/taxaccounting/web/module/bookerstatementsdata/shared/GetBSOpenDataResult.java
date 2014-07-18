package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Dmitriy Levykin
 */
public class GetBSOpenDataResult implements Result {

    // Признак контролера УНП
    // true - контролера УНП, false - контролер, null - не контролер
    private Boolean isControlUNP;

    private String departmentName;
    private String accountPeriodName;
    private String statementsKindName;

    public Boolean getControlUNP() {
        return isControlUNP;
    }

    public void setControlUNP(Boolean isControlUNP) {
        this.isControlUNP = isControlUNP;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getAccountPeriodName() {
        return accountPeriodName;
    }

    public void setAccountPeriodName(String accountPeriodName) {
        this.accountPeriodName = accountPeriodName;
    }

    public String getStatementsKindName() {
        return statementsKindName;
    }

    public void setStatementsKindName(String statementsKindName) {
        this.statementsKindName = statementsKindName;
    }
}
