package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   Action формы настроек подразделений
 *   @author Dmitriy Levykin
 */
public class GetDepartmentTreeDataAction extends UnsecuredActionImpl<GetDepartmentTreeDataResult> implements ActionName {

    TaxType taxType;

    Integer departmentId;

    boolean onlyPeriods;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isOnlyPeriods() {
        return onlyPeriods;
    }

    public void setOnlyPeriods(boolean onlyPeriods) {
        this.onlyPeriods = onlyPeriods;
    }

    @Override
    public String getName() {
        return "Получение дерева подразделений";
    }
}
