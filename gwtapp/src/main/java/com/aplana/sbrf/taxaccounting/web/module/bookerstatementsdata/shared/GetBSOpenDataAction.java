package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   @author Dmitriy Levykin
 */
public class GetBSOpenDataAction extends UnsecuredActionImpl<GetBSOpenDataResult> implements ActionName {

    /** Идентификатор подразделения */
    private Integer departmentId;
    /** Тип бухотчетности (0, 1) */
    private Integer statementsKind;
    private Integer reportPeriodId;

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public Integer getStatementsKind() {
        return statementsKind;
    }

    public void setStatementsKind(Integer statementsKind) {
        this.statementsKind = statementsKind;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public String getName() {
        return "Получение начальных данных";
    }
}
