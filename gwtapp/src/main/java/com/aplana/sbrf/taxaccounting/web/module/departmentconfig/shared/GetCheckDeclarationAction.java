package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   Action формы настроек подразделений
 *   @author Dmitriy Levykin
 */
public class GetCheckDeclarationAction extends UnsecuredActionImpl<GetCheckDeclarationResult> implements ActionName {

    TaxType taxType;
    private Integer period;
    private Integer department;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public Integer getReportPeriodId() {
        return period;
    }

    public void setReportPeriodId(Integer period) {
        this.period = period;
    }

    public Integer getDepartment() {
        return department;
    }

    public void setDepartment(Integer department) {
        this.department = department;
    }

    @Override
    public String getName() {
        return "Получение списка деклараций";
    }
}
