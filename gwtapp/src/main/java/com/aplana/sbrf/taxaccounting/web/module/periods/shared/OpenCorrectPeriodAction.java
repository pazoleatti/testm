package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodViewModel;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.List;

public class OpenCorrectPeriodAction extends UnsecuredActionImpl<OpenCorrectPeriodResult> {
    TaxType taxType;
    List<Integer> selectedDepartments;
    Date term;
    ReportPeriodViewModel selectedPeriod;

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

    public ReportPeriodViewModel getSelectedPeriod() {
        return selectedPeriod;
    }

    public void setSelectedPeriod(ReportPeriodViewModel selectedPeriod) {
        this.selectedPeriod = selectedPeriod;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}
