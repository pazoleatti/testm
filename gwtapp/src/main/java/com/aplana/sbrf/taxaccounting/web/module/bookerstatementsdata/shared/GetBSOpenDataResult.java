package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

/**
 * @author Dmitriy Levykin
 */
public class GetBSOpenDataResult implements Result {

    // Признак контролера УНП
    // true - контролера УНП, false - контролер, null - не контролер
    private Boolean isControlUNP;

    private String departmentName;
    private String reportPeriodName;
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

    public String getReportPeriodName() {
        return reportPeriodName;
    }

    public void setReportPeriodName(String reportPeriodName) {
        this.reportPeriodName = reportPeriodName;
    }

    public String getStatementsKindName() {
        return statementsKindName;
    }

    public void setStatementsKindName(String statementsKindName) {
        this.statementsKindName = statementsKindName;
    }
}
