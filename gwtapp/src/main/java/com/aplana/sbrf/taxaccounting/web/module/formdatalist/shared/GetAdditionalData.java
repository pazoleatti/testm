package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author fmukhametdinov
 */
public class GetAdditionalData extends UnsecuredActionImpl<GetAdditionalDataResult> {

    private Integer typeId;

    private Integer reportPeriodId;
    private int departmentId;
    private TaxType taxType;

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public TaxType getTaxType() {
        return taxType;
    }
}
