package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentCombined;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Dmitriy Levykin
 */
public class GetDepartmentCombinedResult implements Result {

    private DepartmentCombined departmentCombined;

    public DepartmentCombined getDepartmentCombined() {
        return departmentCombined;
    }

    public void setDepartmentCombined(DepartmentCombined departmentCombined) {
        this.departmentCombined = departmentCombined;
    }
}
