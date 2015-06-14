package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Action формы настроек подразделений (удаление)
 * @author vpetrov
 */
public class DeleteDepartmentCombinedAction extends UnsecuredActionImpl<DeleteDepartmentCombinedResult> implements ActionName {

    private DepartmentCombined departmentCombined;
    private Integer period;
    private TaxType taxType;
    private Integer department;
    private String oldUUID;

    @Override
    public String getName() {
        return "Удаление деталей подразделения";
    }

    public DepartmentCombined getDepartmentCombined() {
        return departmentCombined;
    }

    public void setDepartmentCombined(DepartmentCombined departmentCombined) {
        this.departmentCombined = departmentCombined;
    }

    public Integer getReportPeriodId() {
        return period;
    }

    public void setReportPeriodId(Integer period) {
        this.period = period;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public Integer getDepartment() {
        return department;
    }

    public void setDepartment(Integer department) {
        this.department = department;
    }

    public String getOldUUID() {
        return oldUUID;
    }

    public void setOldUUID(String oldUUID) {
        this.oldUUID = oldUUID;
    }
}
