package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.List;

public class CheckCorrectionPeriodStatusAction extends UnsecuredActionImpl<CheckCorrectionPeriodStatusResult> {
    TaxType taxType;
    List<Integer> selectedDepartments;
    Date term;
    int reportPeriodId;

    public List<Integer> getSelectedDepartments() {
        return selectedDepartments;
    }

    public void setSelectedDepartments(List<Integer> selectedDepartments) {
        this.selectedDepartments = selectedDepartments;
    }

    public Date getTerm() {
        return term;
    }

    public void setTerm(Date term) {
        this.term = term;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public int getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(int reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }
}
